package org.dcache.webadmin.controller.impl;

import java.util.ArrayList;
import java.util.List;
import org.dcache.webadmin.controller.PoolGroupService;
import org.dcache.webadmin.controller.exceptions.PoolGroupServiceException;
import org.dcache.webadmin.view.beans.PoolGroupBean;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.dcache.admin.webadmin.datacollector.datatypes.CellStatus;
import org.dcache.webadmin.controller.util.BeanDataMapper;
import org.dcache.webadmin.model.businessobjects.Pool;
import org.dcache.webadmin.model.dataaccess.DAOFactory;
import org.dcache.webadmin.model.dataaccess.DomainsDAO;
import org.dcache.webadmin.model.dataaccess.PoolsDAO;
import org.dcache.webadmin.model.exceptions.DAOException;
import org.dcache.webadmin.view.beans.CellServicesBean;
import org.dcache.webadmin.view.beans.PoolQueueBean;
import org.dcache.webadmin.view.beans.PoolSpaceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jans
 */
public class StandardPoolGroupService implements PoolGroupService {

    private static final Logger _log = LoggerFactory.getLogger(StandardPoolQueuesService.class);
    private DAOFactory _daoFactory;

    public StandardPoolGroupService(DAOFactory daoFactory) {
        _daoFactory = daoFactory;
    }

    public List<PoolGroupBean> getPoolGroups() throws PoolGroupServiceException {
        try {
            Set<Pool> pools = getPoolsDAO().getPools();
            Set<String> poolGroupNames = getPoolsDAO().getPoolGroupNames();
            _log.debug("returned pools: {} returned poolGroups: {}", pools.size(),
                    poolGroupNames.size());
            Map<String, List<String>> domainMap = getDomainsDAO().getDomainsMap();

            Set<CellStatus> cellStatuses = getDomainsDAO().getCellStatuses();

            List<PoolGroupBean> poolGroups = new ArrayList<PoolGroupBean>();
            for (String currentPoolGroupName : poolGroupNames) {
                PoolGroupBean newPoolGroup = createPoolGroupBean(
                        currentPoolGroupName, pools, domainMap, cellStatuses);
                poolGroups.add(newPoolGroup);
            }
            _log.debug("returned PoolGroupBeans: " + poolGroups.size());
            Collections.sort(poolGroups);
            return poolGroups;
        } catch (DAOException e) {
            throw new PoolGroupServiceException(e);
        }

    }

    private PoolsDAO getPoolsDAO() {
        return _daoFactory.getPoolsDAO();
    }

    private DomainsDAO getDomainsDAO() {
        return _daoFactory.getDomainsDAO();
    }

    public void setDAOFactory(DAOFactory daoFactory) {
        _daoFactory = daoFactory;
    }

    private PoolGroupBean createPoolGroupBean(String currentPoolGroupName,
            Set<Pool> pools, Map<String, List<String>> domainMap,
            Set<CellStatus> cellStatuses) {
        List<PoolSpaceBean> poolSpaces = new ArrayList<PoolSpaceBean>();
        List<PoolQueueBean> poolMovers = new ArrayList<PoolQueueBean>();
        List<CellServicesBean> poolStatuses = new ArrayList<CellServicesBean>();
        for (Pool currentPool : pools) {
            if (currentPool.isInPoolGroup(currentPoolGroupName)) {
                poolSpaces.add(createPoolSpaceBean(currentPool, domainMap));
                poolMovers.add(createPoolQueueBean(currentPool, domainMap));
                poolStatuses.add(createCellServiceBean(getMatchingCellStatus(
                        currentPool, cellStatuses)));
            }
        }
        PoolGroupBean newPoolGroup = new PoolGroupBean(currentPoolGroupName,
                poolSpaces, poolMovers);
        newPoolGroup.setCellStatuses(poolStatuses);
        return newPoolGroup;
    }

    private PoolSpaceBean createPoolSpaceBean(Pool pool,
            Map<String, List<String>> domainMap) {
        return BeanDataMapper.poolModelToView(pool, domainMap);
    }

    private PoolQueueBean createPoolQueueBean(Pool pool,
            Map<String, List<String>> domainMap) {
        return BeanDataMapper.poolQueueModelToView(pool, domainMap);
    }

    private CellServicesBean createCellServiceBean(CellStatus cellStatus) {
        return BeanDataMapper.cellModelToView(cellStatus);
    }

    private CellStatus getMatchingCellStatus(Pool pool, Set<CellStatus> cellStatuses) {
        CellStatus result = new CellStatus(pool.getName());
        for (CellStatus cell : cellStatuses) {
            if (cell.getName().equals(pool.getName())) {
                result = cell;
                break;
            }
        }
        return result;
    }
}


