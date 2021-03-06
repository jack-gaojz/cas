package org.apereo.cas.web.flow.login;

import lombok.val;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;

/**
 * This is {@link RedirectUnauthorizedServiceUrlAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedirectUnauthorizedServiceUrlAction extends AbstractAction {
    private final ServicesManager servicesManager;

    @Override
    public Event doExecute(final RequestContext context) {
        val redirectUrl = determineUnauthorizedServiceRedirectUrl(context);
        WebUtils.putUnauthorizedRedirectUrl(context, redirectUrl);
        return null;
    }

    /**
     * Determine unauthorized service redirect url.
     *
     * @param context the context
     * @return the uri
     */
    protected URI determineUnauthorizedServiceRedirectUrl(final RequestContext context) {
        val redirectUrl = WebUtils.getUnauthorizedRedirectUrlIntoFlowScope(context);
        val currentEvent = context.getCurrentEvent();
        val eventAttributes = currentEvent.getAttributes();
        LOGGER.debug("Finalizing the unauthorized redirect URL [{}] when processing event [{}] with attributes [{}]",
            redirectUrl, currentEvent.getId(), eventAttributes);
        return redirectUrl;
    }
}
