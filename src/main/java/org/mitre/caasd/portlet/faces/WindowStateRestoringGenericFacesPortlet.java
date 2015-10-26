package org.mitre.caasd.portlet.faces;


import java.io.IOException;


import javax.faces.render.ResponseStateManager;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.faces.GenericFacesPortlet;


import org.mitre.caasd.portlet.preferences.PortletPreferencesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletURLFactoryUtil;


/**
 * A thin wrapper around <code>GenericFacesPortlet</code> that provides
 * persistent {@link WindowState}. It wraps
 * {@link #doView(RenderRequest, RenderResponse) doView} to persist the most
 * recent <code>WindowState</code> and
 * {@link #processAction(ActionRequest, ActionResponse) processAction} to
 * restore it.
 * 
 * <p>
 * Relevant portlet spec:
 * 
 * <pre>
 * Portlet Spec 2.0:
 * 
 * Page 43, Block 10, Section PLT.5.4.1 Action Request:
 * While processing an action request, the portlet may instruct the portal/portlet-container to
 * redirect the user to a specific URL. If the portlet issues a redirection, when the
 * processAction method concludes, the portal/portlet-container must send the redirection
 * back to the user agentxviii and it must finalize the processing of the client request.
 * 
 * Page 43, Block 15, Section PLT.5.4.1 Action Request:
 * A portlet may change its portlet mode and its window state during an action request. This
 * is done using the ActionResponse object. The change of portlet mode must be effective
 * for the following requests the portlet receives. There are some exceptional circumstances,
 * such as changes of access control privileges that could prevent the portlet mode change
 * from happening. The change of window state should be effective for the following
 * requests the portlet receives. The portlet should not assume that the subsequent request
 * will be in the window state set as the portal/portlet-container could override the window
 * state because of implementation dependencies between portlet modes and window states.
 * 
 * Page 45, Block 10, Section PLT.5.4.3 Render Request:
 * The portlet should not trigger any state changes in a render request and be a safe
 * operation as defined by the HTTP specification (see RFC 2616,
 * http://www.w3.org/Protocols/rfc2616/rfc2616.html).
 * 
 * Page 55, Block 10, Section PLT.7.1.1 BaseURL interface:
 * Portlet developers should note that the parameters of the current
 * render request are not carried over when creating an ActionURL or RenderURL. When
 * creating a ResourceURL the current render parameters are automatically added to that
 * URL by the portlet container, but are hidden to the getParameter calls of the portlet
 * URL object. Setting parameters on an ActionURL will result in action parameters, not
 * render parameters or public render parameters.
 * 
 * Page 56, Block 15, Section PLT 7.1.2 Including a Portlet Mode or a Window State
 * The PortletURL interface has the
 * setWindowState and setPortletMode methods for setting the portlet mode and window
 * state in the portlet URL. For example
 * </pre>
 * 
 * @see GenericFacesPortlet
 */
public class WindowStateRestoringGenericFacesPortlet extends GenericFacesPortlet {
    private static final String KEY_PREFERENCE_WINDOW_STATE = "windowState";
    private static final Logger LOGGER = LoggerFactory.getLogger(
            WindowStateRestoringGenericFacesPortlet.class );

    private WindowState administrativelyConfiguredWindowStateFrom( ThemeDisplay themeDisplay )
            throws SystemException {
        return windowStateFrom( PortletPreferencesFactory.administrativeInstance( themeDisplay ) );
    }

    @Override
    protected void doView( RenderRequest renderRequest, RenderResponse renderResponse )
            throws PortletException, IOException {
        WindowState windowState = renderRequest.getWindowState();
        try {
            PortletPreferences preferences = PortletPreferencesFactory.userGlobal(
                    themeDisplayFrom( renderRequest ) );
            preferences.setValue( KEY_PREFERENCE_WINDOW_STATE, windowState.toString() );
            preferences.store();
        }
        catch ( SystemException e ) {
            if ( LOGGER.isWarnEnabled() ) {
                LOGGER.warn( "unable to persist window state preference: {}",
                        e.getMessage() );
            }
        }

        super.doView( renderRequest, renderResponse );
    };

    @Override
    public void processAction( ActionRequest actionRequest, ActionResponse
            actionResponse )
            throws PortletException, IOException {

        if ( null == actionRequest.getParameter(
                ResponseStateManager.VIEW_STATE_PARAM ) ) {
            // no view state indicates this is not a JSF request
            ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
                    .getAttribute( WebKeys.THEME_DISPLAY );
            LiferayPortletURL portletURL = PortletURLFactoryUtil.create(
                    actionRequest,
                    themeDisplay.getPpid(),
                    themeDisplay.getPlid(),
                    PortletRequest.RENDER_PHASE );
            portletURL.setCopyCurrentRenderParameters( true );

            try {
                portletURL.setWindowState( windowStateFrom( themeDisplay ) );
            }
            catch ( SystemException e ) {
                if ( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "unable to find specified window state: {}",
                            e.getMessage() );
                }
                portletURL.setWindowState( WindowState.MAXIMIZED );
            }

            LOGGER.info( "naked action request, redirecting to {}", portletURL );
            actionResponse.sendRedirect( portletURL.toString() );
        }
        else {
            super.processAction( actionRequest, actionResponse );
        }
    }

    private ThemeDisplay themeDisplayFrom( PortletRequest request ) {
        return (ThemeDisplay) request.getAttribute( WebKeys.THEME_DISPLAY );
    }

    private WindowState userPreferredWindowStateFrom( ThemeDisplay themeDisplay ) throws SystemException {
        // TODO: ensure this respects do as user...
        return windowStateFrom( PortletPreferencesFactory.userGlobal( themeDisplay ) );
    }

    private WindowState windowStateFrom( ThemeDisplay themeDisplay ) throws SystemException {
        WindowState administrativeWindowState = administrativelyConfiguredWindowStateFrom(
                themeDisplay );
        return administrativeWindowState == null
                ? userPreferredWindowStateFrom( themeDisplay )
                : administrativeWindowState;
    }

    private WindowState windowStateFrom( PortletPreferences portletPreferences ) {
        String windowStateName = portletPreferences.getValue( KEY_PREFERENCE_WINDOW_STATE, null );
        return windowStateName == null ? null : new LiferayWindowState( windowStateName );
    }
}
