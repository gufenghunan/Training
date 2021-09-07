package com.training.rest.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.training.util.PartUtil;

import wt.part.WTPart;

@Service("partInfoService")
@Scope("prototype")
public class PartInfoServiceImpl implements PartInfoService{

	@Override
	public PartInfo getPartInfo(String number) {
		WTPart part = PartUtil.getLastestWTPartByNumber(number);
		if(part!=null) {
			PartInfo partinfo = PartInfoImpl.newInstance(part);
			return partinfo;
		}
		return null;
	}

	@Override
	public List<PartInfo> getPartInfos(List<String> numbers) {
		List<PartInfo> infos = new ArrayList<PartInfo>();
		for(String number :numbers) {
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
			if(part!=null) {
				PartInfo partinfo = PartInfoImpl.newInstance(part);
				infos.add(partinfo);
			}
		}
		return infos;
	}

}
