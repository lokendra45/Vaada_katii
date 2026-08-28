package com.gaatho.rent.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIFont
import platform.UIKit.NSFontAttributeName
import platform.UIKit.drawAtPoint
import platform.CoreGraphics.CGPointMake
import platform.UIKit.NSStringDrawingOptions

@OptIn(ExperimentalForeignApi::class)
actual suspend fun generateTenantPdf(
    tenantName: String,
    profileInfo: String,
    rentInfo: String
): ByteArray = withContext(Dispatchers.IO) {
    val pdfData = NSMutableData()
    
    // Create PDF Context (A4 Size: 595 x 842)
    val bounds = CGRectMake(0.0, 0.0, 595.0, 842.0)
    UIGraphicsBeginPDFContextToData(pdfData, bounds, null)
    UIGraphicsBeginPDFPage()
    
    val titleFont = UIFont.boldSystemFontOfSize(20.0)
    val headerFont = UIFont.boldSystemFontOfSize(16.0)
    val regularFont = UIFont.systemFontOfSize(14.0)
    
    var yPosition = 50.0
    
    // Title
    val titleAttr = mapOf<Any?, Any>(NSFontAttributeName to titleFont)
    (NSString.create(string = "Tenant Backup: $tenantName") as NSString).drawAtPoint(CGPointMake(50.0, yPosition), titleAttr)
    yPosition += 40.0
    
    // Profile Info Header
    val headerAttr = mapOf<Any?, Any>(NSFontAttributeName to headerFont)
    (NSString.create(string = "Profile Information:") as NSString).drawAtPoint(CGPointMake(50.0, yPosition), headerAttr)
    yPosition += 25.0
    
    // Profile Info
    val regularAttr = mapOf<Any?, Any>(NSFontAttributeName to regularFont)
    profileInfo.split("\n").forEach { line ->
        (NSString.create(string = line) as NSString).drawAtPoint(CGPointMake(50.0, yPosition), regularAttr)
        yPosition += 20.0
    }
    
    yPosition += 20.0
    
    // Rent Info Header
    (NSString.create(string = "Rent Information:") as NSString).drawAtPoint(CGPointMake(50.0, yPosition), headerAttr)
    yPosition += 25.0
    
    // Rent Info
    rentInfo.split("\n").forEach { line ->
        (NSString.create(string = line) as NSString).drawAtPoint(CGPointMake(50.0, yPosition), regularAttr)
        yPosition += 20.0
    }
    
    UIGraphicsEndPDFContext()
    
    pdfData.bytes?.readBytes(pdfData.length.toInt()) ?: ByteArray(0)
}
