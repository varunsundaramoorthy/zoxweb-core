package org.zoxweb.shared.data;

import org.zoxweb.shared.util.ArrayValues;
import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.NVEntity;
import org.zoxweb.shared.util.SharedUtil;

import java.util.List;

/**
 * Created on 8/11/17
 */
public class AppConfigDAO
        extends SetNameDescriptionDAO {

    public enum Param
        implements GetNVConfig
    {
        APP_ID(NVConfigManager.createNVConfigEntity("app_id", "App ID", "AppID", true, false, AppIDDAO.NVC_APP_ID_DAO, NVConfigEntity.ArrayType.NOT_ARRAY)),
        PROPERTIES(NVConfigManager.createNVConfigEntity("properties", "Properties", "Properties", false, true, NVEntity[].class, NVConfigEntity.ArrayType.GET_NAME_MAP)),

        ;

        private final NVConfig nvc;

        Param(NVConfig nvc)
        {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig()
        {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_APP_CONFIG_DAO = new NVConfigEntityLocal(
            "app_config_dao",
            null,
            "AppConfigDAO",
            true,
            false,
            false,
            false,
            AppConfigDAO.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            SetNameDescriptionDAO.NVC_NAME_DESCRIPTION_DAO
    );


    public AppConfigDAO()
    {
        super(NVC_APP_CONFIG_DAO);
    }

    public AppConfigDAO(AppIDDAO appIDDAO)
    {
        this();
        setAppIDDAO(appIDDAO);
    }

    public AppIDDAO getAppIDDAO() {
        return lookupValue(Param.APP_ID);
    }

    public void setAppIDDAO(AppIDDAO appID) {
        setValue(Param.APP_ID, appID);
    }

    @SuppressWarnings("unchecked")
    public ArrayValues<NVEntity> getProperties()
    {
        return (ArrayValues<NVEntity>) lookup(Param.PROPERTIES);
    }

    @SuppressWarnings("unchecked")
    public void setProperties(ArrayValues<NVEntity> values)
    {
        ArrayValues<NVEntity> properties = (ArrayValues<NVEntity>) lookup(Param.PROPERTIES);
        properties.add(values.values(), true);
    }

    @SuppressWarnings("unchecked")
    public void setProperties(List<NVEntity> values)
    {
        ArrayValues<NVEntity> properties = (ArrayValues<NVEntity>) lookup(Param.PROPERTIES);
        properties.add(values.toArray(new NVEntity[0]), true);
    }

}