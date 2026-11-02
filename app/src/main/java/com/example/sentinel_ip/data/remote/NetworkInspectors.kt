package com.example.sentinel_ip.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection

data class PortScanResult(
    val port: Int,
    val serviceName: String,
    val isOpen: Boolean
)

data class SslCertResult(
    val issuer: String,
    val subject: String,
    val validFrom: String,
    val validTo: String,
    val daysUntilExpiry: Long
)

object NetworkInspectors {

    private val COMMON_PORTS = listOf(
        21 to "FTP",
        22 to "SSH",
        80 to "HTTP",
        443 to "HTTPS",
        3389 to "RDP",
        8080 to "HTTP-Proxy"
    )

    suspend fun resolveReverseDns(ipAddress: String): String? = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ipAddress)
            val hostName = address.hostName
            if (hostName != ipAddress) hostName else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun scanCommonPorts(ipAddress: String): List<PortScanResult> = withContext(Dispatchers.IO) {
        COMMON_PORTS.map { (port, service) ->
            var isOpen = false
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ipAddress, port), 1000)
                    isOpen = true
                }
            } catch (_: Exception) {
                isOpen = false
            }
            PortScanResult(port, service, isOpen)
        }
    }

    suspend fun inspectSslCertificate(targetHost: String): SslCertResult? = withContext(Dispatchers.IO) {
        try {
            val cleanHost = targetHost.replace("https://", "").replace("http://", "").split("/")[0]
            val url = URL("https://$cleanHost")
            val connection = url.openConnection() as HttpsURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()

            val certs = connection.serverCertificates
            if (certs.isNotEmpty() && certs[0] is X509Certificate) {
                val cert = certs[0] as X509Certificate
                val daysLeft = (cert.notAfter.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
                SslCertResult(
                    issuer = cert.issuerDN.name,
                    subject = cert.subjectDN.name,
                    validFrom = cert.notBefore.toString(),
                    validTo = cert.notAfter.toString(),
                    daysUntilExpiry = daysLeft
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
