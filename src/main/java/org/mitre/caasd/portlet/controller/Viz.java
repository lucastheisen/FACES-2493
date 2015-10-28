package org.mitre.caasd.portlet.controller;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletURLFactoryUtil;


@ManagedBean
@RequestScoped
public class Viz {
    private static final Logger LOGGER = LoggerFactory.getLogger( Viz.class );

    private WindowState regularWindowState = WindowState.MAXIMIZED;

    private String buildUrl( WindowState windowState ) {
        LiferayFacesContext context = LiferayFacesContext.getInstance();
        ThemeDisplay themeDisplay = context.getThemeDisplay();
        LiferayPortletURL url = PortletURLFactoryUtil.create(
                LiferayFacesContext.getInstance().getPortletRequest(),
                themeDisplay.getPpid(),
                themeDisplay.getPlid(),
                PortletRequest.RENDER_PHASE );
        try {
            url.setWindowState( windowState );
        }
        catch ( WindowStateException e ) {
            LOGGER.debug( "WindowState [{}] not supported", windowState );
        }
        url.setParameter( "label", context.getRequestParameter( "label" ) );
        url.setParameter( "returnUrl", context.getRequestParameter( "returnUrl" ) );
        return url.toString();
    }

    public String getExclusiveUrl() throws WindowStateException {
        return buildUrl( LiferayWindowState.EXCLUSIVE );
    }

    public String getRegularUrl() throws WindowStateException {
        return buildUrl( regularWindowState );
    }

    public boolean isExclusive() {
        return LiferayFacesContext.getInstance().getThemeDisplay()
                .getPortletDisplay().isStateExclusive();
    }
}
