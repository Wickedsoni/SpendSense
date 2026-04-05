package com.wickedcoder.app.data.parser

import com.wickedcoder.app.domain.model.TxnType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SmsParserEngineTest {

    private lateinit var engine: SmsParserEngine

    @Before
    fun setup() {
        engine = SmsParserEngine()
    }

    // ── Amount extraction ─────────────────────────────────────────────────────

    @Test
    fun `parses amount with Rs dot format`() {
        val result = engine.parse("HDFCBK", "Rs.500.00 debited from a/c XX1234. Info: UPI-Zomato")
        assertNotNull(result)
        assertEquals(500.00, result!!.amount, 0.001)
    }

    @Test
    fun `parses amount with comma separator`() {
        val result = engine.parse("SBIIN", "Your a/c debited by Rs 1,500.00 on 05Apr26 to Swiggy")
        assertNotNull(result)
        assertEquals(1500.00, result!!.amount, 0.001)
    }

    @Test
    fun `parses INR prefix`() {
        val result = engine.parse("AXISBank", "INR 250.00 spent at Amazon via UPI")
        assertNotNull(result)
        assertEquals(250.00, result!!.amount, 0.001)
    }

    // ── TxnType detection ─────────────────────────────────────────────────────

    @Test
    fun `detects DEBIT on debited keyword`() {
        val result = engine.parse("ICICIB", "ICICI Bank Acct XX999 debited for Rs 100 at Swiggy")
        assertEquals(TxnType.DEBIT, result?.txnType)
    }

    @Test
    fun `detects CREDIT on credited keyword`() {
        val result = engine.parse("HDFCBK", "Rs.2000.00 credited to your a/c from Salary")
        assertEquals(TxnType.CREDIT, result?.txnType)
    }

    @Test
    fun `detects CREDIT on refund keyword`() {
        val result = engine.parse("SBIIN", "Refund of Rs 300 received in your a/c from Zomato")
        assertEquals(TxnType.CREDIT, result?.txnType)
    }

    // ── Bank name detection ───────────────────────────────────────────────────

    @Test
    fun `detects HDFC bank from sender`() {
        val result = engine.parse("HDFCBK", "Rs.100.00 debited. Info: UPI-Swiggy")
        assertEquals("HDFC Bank", result?.bankName)
    }

    @Test
    fun `detects SBI bank from sender`() {
        val result = engine.parse("SBIIN", "Your a/c debited by Rs 500 on 05Apr26 to Amazon")
        assertEquals("State Bank of India", result?.bankName)
    }

    @Test
    fun `detects ICICI bank from sender`() {
        val result = engine.parse("ICICIB", "ICICI Bank Acct XX123 debited for Rs 200 at Blinkit")
        assertEquals("ICICI Bank", result?.bankName)
    }

    // ── Merchant extraction ───────────────────────────────────────────────────

    @Test
    fun `extracts merchant from Info field`() {
        val result = engine.parse("HDFCBK", "Rs.150.00 debited from a/c XX1234. Info: UPI-Zomato")
        assertEquals("Zomato", result?.merchantRaw)
    }

    @Test
    fun `extracts merchant from at keyword`() {
        val result = engine.parse("AXISBK", "INR 99.00 spent at Netflix via UPI")
        assertNotNull(result?.merchantRaw)
        assert(result!!.merchantRaw.contains("Netflix", ignoreCase = true))
    }

    @Test
    fun `extracts UPI VPA as merchant fallback`() {
        val result = engine.parse("SBIIN", "Rs 50 debited. UPI Ref:123456789. merchant@paytm")
        assertNotNull(result)
        assertEquals("merchant@paytm", result?.merchantRaw)
    }

    // ── Non-transaction SMS ───────────────────────────────────────────────────

    @Test
    fun `returns null for OTP sms`() {
        val result = engine.parse("HDFCBK", "Your OTP for login is 482910. Do not share with anyone.")
        assertNull(result)
    }

    @Test
    fun `returns null for promotional sms`() {
        val result = engine.parse("ZOMATO", "Get 50% off on your next order! Use code SAVE50.")
        assertNull(result)
    }

    @Test
    fun `returns null for empty body`() {
        val result = engine.parse("HDFCBK", "")
        assertNull(result)
    }
}
