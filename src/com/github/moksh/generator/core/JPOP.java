package com.github.moksh.generator.core;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.github.moksh.generator.GUI.Utils.CommonUtils;

public class JPOP {
private String op;
private String invoke;
private String from;
private String path;
private String value;
private List<JPOP> input;
private List<JPOP> output;
private String condition;
private String function;
private String comment;
private Integer order;
private String name="";
private String follow="";
public Integer getOrder() {
	return order;
}
public void setOrder(Integer order) {
	this.order = order;
}
public String getName() {
	return name;
}
public void setName(String uniqueName) {
	this.name = uniqueName;
}

public String getOp() {
	return op;
}
public void setOp(String op) {
	this.op = op;
}
public String getInvoke() {
	return invoke;
}
public void setInvoke(String invoke) {
	this.invoke = invoke;
}
public String getFrom() {
	return from;
}
public void setFrom(String from) {
	this.from = from;
}
public String getPath() {
	return path;
}
public void setPath(String path) {
	this.path = path;
}
public String getValue() {
	return value;
}

public void setValue(Object value) {
	this.value = value.toString();
}
public List<JPOP> getInput() {
	return input;
}
public void setInput(List<JPOP> input) {
	this.input = input;
}
public List<JPOP> getOutput() {
	return output;
}
public void setOutput(List<JPOP> output) {
	this.output = output;
}

private String json="";

private void appendln(String str) {
	json+=str+"\n";
}
private void append(String str) {
	json+=str;
}
@Override
public String toString(){
	return (toJsonPatchString(false)+"END-OF-LIST").replaceAll(",END-OF-LIST", "");
}
public String toSaveString(){
	return (toJsonPatchString(false)+"END-OF-LIST").replaceAll(",END-OF-LIST", "");
}
private String toJsonPatchString(boolean encode){
	appendln("{");
	appendln("\"op\" : \""+op+"\",");
	if(invoke!=null && invoke.trim().length()>0)
		append("\"invoke\" : \""+invoke+"\",");
	
	if(from!=null && from.trim().length()>0)
		append("\"from\" : \""+from+"\",");
	
	if(path!=null && path.trim().length()>0)
		append("\"path\" : \""+path+"\",");
	
	if(name!=null && name.trim().length()>0)
		append("\"name\" : \""+name+"\",");
	
	if(order!=null && order>=0)
		append("\"order\" : "+order+",");
	
	if(follow!=null && follow.trim().length()>0)
		append("\"follow\" : \""+follow+"\",");
	
	if(value!=null && value.trim().length()>0) {
		String tmpVal=value;
		if(encode)
			tmpVal=CommonUtils.encodeBase64(tmpVal);
		if(!value.contains(":") || encode)
			tmpVal="\""+tmpVal+"\"";
		append("\"value\" : "+tmpVal+",");
	}
	
	if(condition!=null && condition.trim().length()>0)
		if(encode)
			append("\"condition\" : \""+CommonUtils.encodeBase64(condition)+"\",");
		else
			append("\"condition\" : \""+condition+"\",");
	
	if(comment!=null && comment.trim().length()>0)
		if(encode)
			append("\"comment\" : \""+ CommonUtils.encodeBase64(comment)+"\",");
		else
			append("\"comment\" : \""+ comment+"\",");
	
	if(function!=null && function.trim().length()>0)
		if(encode)
			append("\"function\" : \""+ CommonUtils.encodeBase64(function)+"\",");
		else
			append("\"function\" : \""+ function+"\",");
	
	if(input!=null && input.size()>0) {
		appendln("\"input\" : [");
		for (JPOP jpop : input) {
			appendln(jpop.toJsonPatchString(encode));
		}
		appendln("END-OF-LIST");
		append("],");
	}
	if(output!=null && output.size()>0) {
		appendln("\"output\" : [");
		for (JPOP jpop : output) {
			appendln(jpop.toJsonPatchString(encode));
		}
		appendln("END-OF-LIST");
		append("],");
	}
	appendln("END-OF-LIST");
	append("},");
	String tempJson=json;
	json="";
	return tempJson;
}
public String getCondition() {
	return condition;
}
public void setCondition(String condition) {
	this.condition = condition;
}

public String getComment() {
	return comment;
}
public void setComment(String comment) {
	this.comment = comment;
}
public String getFunction() {
	return function;
}
public void setFunction(String function) {
	this.function = function;
}
public String getFollow() {
	return follow;
}
public void setFollow(String follow) {
	this.follow = follow;
}
}
