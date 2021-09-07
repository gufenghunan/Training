package com.training.mvc.builder;

import java.util.ArrayList;
import java.util.List;

import com.ptc.core.components.descriptor.DescriptorConstants.ColumnIdentifiers;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.training.util.GenericUtil;
import com.training.util.PartUtil;

import ext.ptc.xworks.examples.ecn.util.PartConst;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.util.WTException;

@ComponentBuilder(value = { "com.training.mvc.builder.TrainingTableBuilder" })
public class TrainingTableBuilder extends AbstractComponentBuilder {

	private final static String tableId = "com.training.mvc.builder.TrainingTableBuilder";

	@Override
	public Object buildComponentData(ComponentConfig config, ComponentParams params) throws Exception {
		List<Persistable> listpist = new ArrayList<Persistable>();
		String poid = params.getParameter("oid").toString();
		EPMDocument epmdoc = (EPMDocument) PartUtil.getObjectByOid(poid);
		Boolean flag = (Boolean) GenericUtil.getObjectAttributeValue(epmdoc, "CustomPart");
		if (flag) {
			listpist.add(epmdoc);
		}
		return listpist;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams params) throws WTException {
		ComponentConfigFactory factory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig) factory.newTableConfig();
		tableConfig.setActionModel("exportReport");
		tableConfig.setId(tableId);
		tableConfig.setSelectable(true);
		tableConfig.setLabel("定制1");

		ColumnConfig partIcon = factory.newColumnConfig(ColumnIdentifiers.ICON, false);
		tableConfig.addComponent(partIcon);

		ColumnConfig partName = factory.newColumnConfig(ColumnIdentifiers.NAME, true);
		tableConfig.addComponent(partName);

		ColumnConfig partNumber = factory.newColumnConfig(ColumnIdentifiers.NUMBER, true);
		tableConfig.addComponent(partNumber);

		ColumnConfig partVersion = factory.newColumnConfig(ColumnIdentifiers.VERSION, true);
		tableConfig.addComponent(partVersion);

		ColumnConfig partOwner = factory.newColumnConfig(ColumnIdentifiers.OWNER, true);
		partOwner.setLabel("关联");
		tableConfig.addComponent(partOwner);

		ColumnConfig state = factory.newColumnConfig(ColumnIdentifiers.STATE, true);
		state.setLabel("状态");
		tableConfig.addComponent(state);

		return tableConfig;
	}
}
