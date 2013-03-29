package me.sonar.sdkmanager.web

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.slf4j.MDC
import me.sonar.sdkmanager.core.ScalaGoodies._

/**
 * A servlet filter that inserts various values retrieved from the incoming http
 * request into the MDC.
 * <p/>
 * <p/>
 * The values are removed after the request is processed.
 *
 */
class MDCInsertingServletFilter extends Filter {
    final val RequestMDCKey = "requestId"

    def destroy() {
    }

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        insertIntoMDC(request)
        try {
            chain.doFilter(request, response)
        }
        finally {
            clearMDC()
        }
    }

    private def insertIntoMDC(request: ServletRequest) {
        MDC.put(RequestMDCKey, compactGUID())
    }

    private def clearMDC() {
        MDC.remove(RequestMDCKey)
    }

    def init(arg0: FilterConfig) {
    }
}
