package com.example.stallalerterapp.repo

import android.net.Uri
import com.example.stallalerterapp.data.csv.Candidate
import java.util.Optional

/** CSV repository abstraction.
 *  Separates the data source from the UI layer,
 *  enabling constructor injection and pure Kotlin business rules. */
interface CsvRepository {
    /** Import candidates from a CSV file selected via Storage Access Framework.
     *  @return Result containing List<Candidate> on success, or ImportError on failure. */
    fun importCsv(uri: Uri): Result<List<Candidate>>
}