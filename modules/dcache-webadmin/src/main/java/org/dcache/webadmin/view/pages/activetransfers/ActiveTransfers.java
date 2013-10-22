package org.dcache.webadmin.view.pages.activetransfers;

import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import org.dcache.webadmin.controller.ActiveTransfersService;
import org.dcache.webadmin.controller.exceptions.ActiveTransfersServiceException;
import org.dcache.webadmin.view.beans.ActiveTransfersBean;
import org.dcache.webadmin.view.pages.basepage.SortableBasePage;
import org.dcache.webadmin.view.panels.activetransfers.ActiveTransfersPanel;
import org.dcache.webadmin.view.util.Role;
import org.dcache.webadmin.view.util.SelectableWrapper;

public class ActiveTransfers extends SortableBasePage {

    private static final Logger _log = LoggerFactory.getLogger(ActiveTransfers.class);
    private static final long serialVersionUID = -1360523434922193867L;

    public ActiveTransfers() {
        Form activeTransfersForm = new Form("activeTransfersForm");
        activeTransfersForm.add(new FeedbackPanel("feedback"));
        Button button = new SubmitButton("submit");
        MetaDataRoleAuthorizationStrategy.authorize(button, RENDER, Role.ADMIN);
        activeTransfersForm.add(button);
        getActiveTransfers();
        activeTransfersForm.add(new ActiveTransfersPanel("activeTransfersPanel",
                new PropertyModel(this, "activeTransfers")));
        add(activeTransfersForm);
    }

    private ActiveTransfersService getActiveTransfersService() {
        return getWebadminApplication().getActiveTransfersService();
    }

    public List<SelectableWrapper<ActiveTransfersBean>> getActiveTransfers() {
        try {
            _log.debug("getActiveTransfers called");
            return getActiveTransfersService().getActiveTransferBeans();
        } catch (ActiveTransfersServiceException ex) {
            this.error(getStringResource("error.getActiveTransfersFailed") + ex.getMessage());
            _log.debug("getActiveTransfers failed {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private class SubmitButton extends Button {

        private static final long serialVersionUID = -1564058161768591840L;

        public SubmitButton(String id) {
            super(id);
        }

        @Override
        public void onSubmit() {
            try {
                _log.debug("Kill Movers submitted");
                getActiveTransfersService().killTransfers(getActiveTransfers());
            } catch (ActiveTransfersServiceException e) {
                _log.info("couldn't kill some movers - jobIds: {}",
                        e.getMessage());
                error(getStringResource("error.notAllMoversKilled"));
            }
            getActiveTransfers();
        }
    }
}
