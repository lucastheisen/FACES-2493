package org.mitre.caasd.portlet.preferences;


import javax.portlet.PortletPreferences;


import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;


/**
 * The factory methods from this class get their base information from
 * <code>PortletPreferencesFactoryImpl</code>. Specifically the logic around
 * mapping the <i>liferay-portlet.xml</i> parameters:
 * 
 * <ul>
 * <li><a href=
 * "http://docs.liferay.com/portal/6.2/definitions/liferay-portlet-app_6_2_0.dtd.html#preferences-company-wide"
 * >preferences-company-wide</a></li>
 * <li><a href=
 * "http://docs.liferay.com/portal/6.2/definitions/liferay-portlet-app_6_2_0.dtd.html#preferences-unique-per-layout"
 * >preferences-unique-per-layout</a></li>
 * <li><a href=
 * "http://docs.liferay.com/portal/6.2/definitions/liferay-portlet-app_6_2_0.dtd.html#preferences-owned-by-group"
 * >preferences-owned-by-group</a></li>
 * </ul>
 * 
 * to their corresponding PortletPreferences objects.
 * 
 * @see <a
 *      href="http://docs.liferay.com/portal/6.2/definitions/liferay-portlet-app_6_2_0.dtd.html">liferay-portlet.xml
 *      6.2 DTD</a>
 * @see com.liferay.portlet.PortletPreferencesFactoryImpl#getPortletPreferencesIds(long,
 *      long, com.liferay.portal.model.Layout, String, boolean)
 *      PortletPreferencesFactoryImpl.getPortletPreferencesIds(scopeGroupid,
 *      userId, layout, portletId, modeEditGuest )
 */
public class PortletPreferencesFactory {
    /**
     * Returns <code>PortletPreferences</code> set by an administrator for a
     * single instance.
     * 
     * <pre>
     * &lt;preferences-company-wide&gt;true&lt;/preferences-company-wide&gt;
     * &lt;preferences-unique-per-layout&gt;true&lt;/preferences-unique-per-layout&gt;
     * &lt;preferences-owned-by-group&gt;true&lt;/preferences-owned-by-group&gt;
     * </pre>
     * 
     * @param themeDisplay
     *            the ThemeDisplay
     * @return preferences set by an administrator for a single instance
     * @throws SystemException
     *             if unable to load the preferences
     */
    public static PortletPreferences administrativeInstance( ThemeDisplay themeDisplay )
            throws SystemException {
        // check for param p_p_id set if not added to page
        String portletId = themeDisplay.getPpid();
        if ( portletId == null || portletId.isEmpty() ) {
            // otherwise, use the portlet itself
            portletId = themeDisplay.getPortletDisplay().getId();
        }

        return PortletPreferencesLocalServiceUtil.getService().getPreferences(
                themeDisplay.getCompanyId(),
                PortletKeys.PREFS_OWNER_ID_DEFAULT,
                PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
                themeDisplay.getPlid(),
                portletId );
    }

    /**
     * Preferences set for the current user, applicable to all instances.
     * 
     * <pre>
     * &lt;preferences-company-wide&gt;false&lt;/preferences-company-wide&gt;
     * &lt;preferences-unique-per-layout&gt;false&lt;/preferences-unique-per-layout&gt;
     * &lt;preferences-owned-by-group&gt;false&lt;/preferences-owned-by-group&gt;
     * </pre>
     * 
     * @param themeDisplay
     *            the ThemeDisplay
     * @return preferences set by an administrator for a single instance
     * @throws SystemException
     *             if unable to load the preferences
     */
    public static PortletPreferences userGlobal( ThemeDisplay themeDisplay )
            throws SystemException {
        // check for param p_p_id set if not added to page
        String rootPortletId = null;
        String ppid = themeDisplay.getPpid();
        if ( ppid == null || ppid.isEmpty() ) {
            // otherwise, use the portlet itself
            rootPortletId = themeDisplay.getPortletDisplay().getRootPortletId();
        }
        else {
            rootPortletId = PortletConstants.getRootPortletId( ppid );
        }

        return PortletPreferencesLocalServiceUtil.getService().getPreferences(
                themeDisplay.getCompanyId(),
                themeDisplay.getUserId(),
                PortletKeys.PREFS_OWNER_TYPE_USER,
                PortletKeys.PREFS_PLID_SHARED,
                rootPortletId );
    }
}
