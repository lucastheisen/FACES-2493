package org.mitre.caasd.portlet.controller;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;


import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletURLFactoryUtil;


@ManagedBean
@RequestScoped
public class Menu {
    private static final String PORTLET_ID_TEST_JSF_PORTLET = "testjsfportlet_WAR_testjsfportlet";

    public List<Item> items;

    private ThemeDisplay themeDisplay;

    public Menu() {
        themeDisplay = LiferayFacesContext.getInstance().getThemeDisplay();
        items = new ArrayList<Item>();
        items.add( new Item( "special sauce" ) );
        items.add( new Item( "lettuce cheese" ) );
        items.add( new Item( "pickles onions" ) );
        items.add( new Item( "sesame seed bun" ) );
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList( items );
    }

    public class Item {
        private String label;

        Item( String label ) {
            this.label = label;
        }

        public String getActionUrl() throws WindowStateException {
            LiferayPortletURL url = PortletURLFactoryUtil.create(
                    LiferayFacesContext.getInstance().getPortletRequest(),
                    PORTLET_ID_TEST_JSF_PORTLET,
                    themeDisplay.getPlid(),
                    PortletRequest.ACTION_PHASE );
            url.setWindowState( WindowState.NORMAL );
            url.setParameter( "label", label );
            url.setParameter( "returnUrl", themeDisplay.getURLCurrent() );
            return url.toString();
        }

        public String getLabel() {
            return label;
        }
    }
}
