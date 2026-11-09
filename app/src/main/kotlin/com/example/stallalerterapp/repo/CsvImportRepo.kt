package com.example.stallalerterapp.repo

import android.content.ContentResolver
import android.net.Uri
import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate

/** CSV import repository.
 *  Reads CSV files via Android's ContentResolver (Storage Access Framework).
 *  Pure Kotlin business rules, constructor-injected dependencies. */
class CsvImportRepo(private val contentResolver: ContentResolver) : CsvRepository {

    /** Import candidates from a CSV file selected via Storage Access Framework.
     *  @return Result containing List<Candidate> on success, or exception on failure. */
    override fun importCsv(uri: Uri): Result<List<Candidate>> {
        return try {
            val content = contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).readText()
            } ?: throw java.io.FileNotFoundException("Could not open the selected file")

            Result.success(parseCsv(content))
        } catch (e: java.io.IOException) {
            Result.failure(RuntimeException("Failed to read CSV file: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Parse CSV content into a list of Candidates.
     *  @return List<Candidate> if successful
     *  @throws IllegalArgumentException if required columns are missing
     */
    fun parseCsv(content: String): List<Candidate> {
        return CsvParser.parse(content)
    }

    companion object {
        // Parser delegated to CsvParser for testability
    }
}

/** Pure Kotlin CSV parser exposed for unit testing. */
object CsvParser {
    @JvmStatic
    fun parse(content: String): List<Candidate> {
        return try {
            val cleaned = content.replace("﻿", "")

            val lines = cleaned.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList()

            if (lines.isEmpty()) {
                emptyList()
            } else {
                val header = lines[0].split(",")
                val colMap = mutableMapOf<String, Int>()
                header.forEachIndexed { index, name ->
                    colMap[name.trim().lowercase()] = index
                }

                val required = listOf("full_name", "stage", "last_contact")
                val missing = required.filter { it !in colMap.keys }
                if (missing.isNotEmpty()) {
                    throw IllegalArgumentException(
                        "Invalid column names — expected full_name, stage, last_contact"
                    )
                } else {
                    val emailCol = colMap["email"]
                    val phoneCol = colMap["phone"]
                    val candidates = mutableListOf<Candidate>()

                    for (i in 1 until lines.size) {
                        val row = lines[i].split(",")
                        val fullName = row.getOrNull(colMap["full_name"] ?: 0)?.trim()
                        val stageStr = row.getOrNull(colMap["stage"] ?: 0)?.trim()
                        val lastContactStr = row.getOrNull(colMap["last_contact"] ?: 0)?.trim()

                        if (fullName?.isBlank() == true) continue

                        val stage = when (stageStr) {
                            "screening" -> Stage.screening
                            "technical-interview" -> Stage.technicalInterview
                            "on-site" -> Stage.onSite
                            "offer" -> Stage.offer
                            "rejected" -> Stage.rejected
                            "hired" -> Stage.hired
                            else -> continue
                        }

                        val lastContact = when (lastContactStr) {
                            null, "" -> continue
                            else -> {
                                try {
                                    LocalDate.parse(lastContactStr)
                                } catch (e: Exception) {
                                    continue
                                }
                            }
                        }

                        candidates.add(Candidate(
                            fullName = fullName!!,
                            stage = stage,
                            lastContact = lastContact,
                            email = row.getOrNull(emailCol ?: -1)?.takeIf { it.isNotBlank() },
                            phone = row.getOrNull(phoneCol ?: -1)?.takeIf { it.isNotBlank() }
                        ))
                    }

                    if (candidates.isEmpty()) {
                        emptyList()
                    } else {
                        candidates
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }
}
