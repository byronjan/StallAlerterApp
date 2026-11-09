package com.example.stallalerterapp.repo

import com.example.stallalerterapp.data.csv.Candidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.AssertionError

/** Unit tests for [CsvImportRepo] CSV-parsing behavior. */
class CsvImportRepoTest {

    @Test
    fun `parseCsv reads email and phone columns into candidates`() {
        val csv = """full_name,stage,last_contact,email,phone
Alice Johnson,screening,2025-01-15,alice@example.com,555-1234
Bob Smith,technical-interview,2025-02-20,bob@example.com,555-5678"""

        val candidates = CsvParser.parse(csv)

        assertEquals(2, candidates.size)
        assertEquals("alice@example.com", candidates[0].email)
        assertEquals("555-1234", candidates[0].phone)
        assertEquals("bob@example.com", candidates[1].email)
        assertEquals("555-5678", candidates[1].phone)
    }

    @Test
    fun `parseCsv returns null for email and phone when those columns are absent`() {
        val csv = """full_name,stage,last_contact
Alice Johnson,screening,2025-01-15"""

        val candidates = CsvParser.parse(csv)

        assertEquals(1, candidates.size)
        assertNull(candidates[0].email)
        assertNull(candidates[0].phone)
    }

    @Test
    fun `parseCsv treats blank email and phone as null rather than empty strings`() {
        val csv = """full_name,stage,last_contact,email,phone
Alice Johnson,screening,2025-01-15,,
Bob Smith,offer,2025-03-01,bob@example.com,"""

        val candidates = CsvParser.parse(csv)

        assertEquals(2, candidates.size)
        assertNull(candidates[0].email)
        assertNull(candidates[0].phone)
        assertEquals("bob@example.com", candidates[1].email)
        assertNull(candidates[1].phone)
    }

    @Test
    fun `parseCsv skips rows where full name is empty`() {
        val csv = """full_name,stage,last_contact,email,phone
,screening,2025-01-15,skip@example.com,555-0000
Valid Name,offer,2025-03-01,valid@example.com,555-1111"""

        val candidates = CsvParser.parse(csv)

        assertEquals(1, candidates.size)
        assertEquals("Valid Name", candidates[0].fullName)
    }

    @Test
    fun `parseCsv drops rows with invalid dates while keeping valid ones`() {
        val csv = """full_name,stage,last_contact,email,phone
Good Person,screening,2025-01-15,good@example.com,555-1111
Bad Date,on-site,not-a-date,bad@example.com,555-2222
Another Good,offer,2025-06-01,another@example.com,555-3333"""

        val candidates = CsvParser.parse(csv)

        assertEquals(2, candidates.size)
        assertEquals("Good Person", candidates[0].fullName)
        assertEquals("good@example.com", candidates[0].email)
        assertEquals("Another Good", candidates[1].fullName)
    }

    @Test
    fun `parseCsv ignores extra columns beyond the expected schema`() {
        val csv = """full_name,stage,last_contact,email,phone,date_added,notes,recruiter
Marcus Reed,technical-interview,2025-11-01,marcus@example.com,555-9012,2025-09-15,Strong backend engineer,Rebecca"""

        val candidates = CsvParser.parse(csv)

        assertEquals(1, candidates.size)
        assertEquals("marcus@example.com", candidates[0].email)
        assertEquals("555-9012", candidates[0].phone)
    }

    @Test
    fun `parseCsv returns empty list for header-only file`() {
        val csv = """full_name,stage,last_contact,email,phone"""

        val candidates = CsvParser.parse(csv)

        assertEquals(0, candidates.size)
    }

    @Test
    fun `parseCsv returns empty list when all rows have invalid dates`() {
        val csv = """full_name,stage,last_contact,email,phone
Invalid Date 1,technical-interview,invalid-date,email@example.com,555-0000
Invalid Date 2,offer,invalid-date,another@example.com,555-1111"""

        val candidates = CsvParser.parse(csv)

        assertEquals(0, candidates.size)
    }

    @Test
    fun `parseCsv returns empty list when all rows have invalid stage values`() {
        val csv = """full_name,stage,last_contact,email,phone
Invalid Stage 1,invalid-stage,2025-01-15,email1@example.com,555-0000
Invalid Stage 2,invalid-stage,2025-02-20,email2@example.com,555-1111"""

        val candidates = CsvParser.parse(csv)

        assertEquals(0, candidates.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseCsv throws IllegalArgumentException for missing required columns`() {
        val csv = """candidate_name,hiring_status,last_interaction
Alice Johnson,screening,2025-01-15
Bob Smith,technical-interview,2025-02-20"""

        CsvParser.parse(csv)
    }
}
