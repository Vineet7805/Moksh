package com.github.moksh.generator.core;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.github.moksh.generator.GUI.Utils.CommonUtils;

public class JsonPatchMap {
	public TreeItem itemLeftSelected =null;
	public TreeItem itemRightSelected =null;
	private JPOP jsonPatchOP;
	public JPOP getJsonPatchOP() {
		if(itemLeftSelected!=null) {
			jsonPatchOP.setFrom(getFrom());
		}
		if(itemRightSelected!=null) {
			jsonPatchOP.setPath(getPath());
		}
		return jsonPatchOP;
	}

	public JsonPatchMap(TreeItem itemLeftSelected, TreeItem itemRightSelected, String op) {
		//super();
		this.itemLeftSelected = itemLeftSelected;
		this.itemRightSelected = itemRightSelected;
		jsonPatchOP=new JPOP();
		if(itemLeftSelected!=null) {
			jsonPatchOP.setFrom(getFrom());
		}
		if(itemRightSelected!=null) {
			jsonPatchOP.setPath(getPath());
		}
		jsonPatchOP.setOp(op);
	}
	
	public JsonPatchMap(JPOP jpop, Tree treeLeft, Tree treeRight) {
		//super();
		jsonPatchOP=jpop;
		if(jpop.getOp().equalsIgnoreCase("add")) {
			itemLeftSelected = null;//CommonUtils.geTreeItemAt(jpop.getPath(), treeLeft);
			itemRightSelected = CommonUtils.geTreeItemAt(jpop.getPath(), treeLeft);
			if(itemRightSelected==null)
				itemRightSelected = CommonUtils.geTreeItemAt(jpop.getPath(), treeRight);
		}else {
			itemLeftSelected = CommonUtils.geTreeItemAt(jpop.getFrom(), treeLeft);
			itemRightSelected = CommonUtils.geTreeItemAt(jpop.getPath(), treeRight);
		}
		//jsonPatchOP=new JPOP();
		if(itemLeftSelected!=null) {
			jsonPatchOP.setFrom(getFrom());
		}
		if(itemRightSelected!=null) {
			jsonPatchOP.setPath(getPath());
		}
		//jsonPatchOP.setOp(op);
	}
	
	private String getFrom() {
		return CommonUtils.getXPath(itemLeftSelected,0,1);
	}
	
	private String getPath() {
		return CommonUtils.getXPath(itemRightSelected,0,1);
	}
}
