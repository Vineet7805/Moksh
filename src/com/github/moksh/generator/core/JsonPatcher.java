package com.github.moksh.generator.core;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import com.github.moksh.generator.GUI.Utils.CommonUtils;

public class JsonPatcher {

	private static final String COPY = "[{\"op\":\"copy\",\"from\":\"#src\",\"path\":\"#dest\"}]";
	private static final String ADD = "[{\"op\":\"add\",\"path\":\"#dest\",\"value\":#value}]";
	private static final String REMOVE = "[{\"op\":\"remove\",\"path\":\"#dest\"}]";
	private static final ObjectMapper om = new ObjectMapper();

	public static JsonNode apply(JPOP jpatch, String target, Map<String, List<JPOP>> nestedOpMap) throws Exception {
		JsonNode jnTarget = om.readTree(target);
		jnTarget = apply(jpatch, jnTarget, nestedOpMap);
		return jnTarget;
	}

	public static JsonNode apply(JPOP jPatch, JsonNode jnTarget, Map<String, List<JPOP>> nestedOpMap) throws Exception {
		//System.out.println("Starting JsonPatcher------------------------------");
		switch (jPatch.getOp()) {
		case "copy":
			//System.out.println("Jsonpatcher apply copy operation----------------------------");
			return copy(jPatch.getName(), jPatch.getFrom(), jPatch.getPath(), jnTarget, jPatch.getCondition(),
					jPatch.getFunction(), nestedOpMap);
		case "add":
			//System.out.println("Jsonpatcher apply add operation----------------------------");
			return add(jPatch.getPath(), jPatch.getValue(), jnTarget);
		}
		return null;
	}

	public static JsonNode add(String path, String value, JsonNode jnTarget) throws Exception {
		boolean settingArrayMembers = true;
		if (path.endsWith("/*"))
			settingArrayMembers = false;
		String pathArray = path;
		if (path.contains("/*"))
			pathArray = path.split(Pattern.quote("/*"))[0];

		value = CommonUtils.placeXPathValue(value, jnTarget);
		String addPatch = ADD.replace("#dest", pathArray);
		if (!value.contains(":"))
			value = "\"" + value + "\"";
		addPatch = addPatch.replace("#value", value);
		System.out.println("Add Patch for array:\n" + addPatch);
		jnTarget = JsonPatch.apply(om.readTree(addPatch), jnTarget);
		return jnTarget;
	}

	public static JsonNode copy(String name, String from, String path, JsonNode jnTarget, String condition,
			String function, Map<String, List<JPOP>> nestedOpMap) throws Exception {
		//System.out.println("from: '"+from+"' to: '"+path+"' ");
		boolean mappingArrayMembers = true;
		ScriptEngineManager factory = null;
		ScriptEngine engine = null;
		if (from.endsWith("/*") && path.endsWith("/*"))
			mappingArrayMembers = false;
		factory = new ScriptEngineManager();
		engine = factory.getEngineByName("JavaScript");
		if (function != null && function.trim().length() > 0)
			engine.eval("function enrich(json){\r\n" + function + "\r\n return json;}");

		String fromArray = null, pathArray = null;
		if (from.contains("/*"))
			fromArray = from.split(Pattern.quote("/*"))[0];
		if (path.contains("/*"))
			pathArray = path.split(Pattern.quote("/*"))[0];
		if (fromArray != null && pathArray != null) {// patch array
			String value = jnTarget.at(pathArray + "/0").toPrettyString();
			JsonNode jnFromArray = jnTarget.at(fromArray);
			JsonNode jnPathArray = jnTarget.at(pathArray);

			if (!mappingArrayMembers) {
				String rPatch = REMOVE.replace("#dest", pathArray);
				jnTarget = JsonPatch.apply(om.readTree(rPatch), jnTarget);
				String addPatch = ADD.replace("#dest", pathArray);
				addPatch = addPatch.replace("#value", "[]");
				//System.out.println("Add Patch for array:\n" + addPatch);
				jnTarget = JsonPatch.apply(om.readTree(addPatch), jnTarget);
			}

			int size = jnFromArray.size();
			//System.out.println("Size of from array is " + size);
			int skipCount = 0;
			for (int i = 0; i < size; i++) {
				//System.out.println("Copy element no. #" + i+" : skipped: "+(i - skipCount));
				String fromXP = from.replace("/*", "/" + i);
				String pathXP = path.replace("/*", "/" + (i - skipCount));
				// if(!from.endsWith("/*")) {
				String cond = condition;
				if (condition != null && condition.contains("#this")) {
					cond = condition.replace("#this", fromXP);
				}
				String json = jnTarget.at(fromXP).toPrettyString();
				engine.eval("json={\"object\":" + json + "};");
				//if (!mappingArrayMembers) {
					boolean skipMapping = false;
					if (cond != null && cond.trim().length() > 0)
						skipMapping = !CommonUtils.evaluateCondition(cond, jnTarget, engine);
					if (skipMapping) {
						skipCount++;
						continue;
					}
				//} 				
				String jPatch = null;
				//adding empty element to the target json body
				if ((i - skipCount) == jnTarget.at(pathArray).size() && mappingArrayMembers) {
					jPatch = ADD.replace("#dest", pathArray + "/" + (i - skipCount));
					//value=json;
					if(value==null || value.trim().length()==0)
						value="\"\"";
					jPatch = jPatch.replace("#value", value);
					jnTarget = JsonPatch.apply(om.readTree(jPatch), jnTarget);
					//System.out.println("Adding new index: "+(i - skipCount));
				}//-------------------------------------------------
				if (function != null && function.trim().length() > 0) {
					String jsonValue = (String) engine.eval("JSON.stringify(enrich(json));");
					JsonNode jnJs = om.readTree(jsonValue).at("/object");
					String jPatchFun = null;
					if (path.endsWith("/*"))
						jPatchFun = ADD.replace("#dest", pathArray + "/" + (i - skipCount));
					else
						jPatchFun = ADD.replace("#dest", pathXP);
					jPatchFun = jPatchFun.replace("#value", jnJs.toPrettyString());
					jnTarget = JsonPatch.apply(om.readTree(jPatchFun), jnTarget);
				}else {
					jPatch = COPY.replace("#src", from.replace("/*", "/" + i));
					jPatch = jPatch.replace("#dest", path.replace("/*", "/" + (i - skipCount)));
					jnTarget = JsonPatch.apply(om.readTree(jPatch), jnTarget);
				}
				//--------------nested-------------------
				if(name!=null) {
					List<JPOP> list=nestedOpMap.get(name);
					if(list!=null) {
						for (JPOP jpop : list) {
							String nestedFrom = jpop.getFrom();
							if(nestedFrom.startsWith(fromArray + "/0"))
								jpop.setFrom(nestedFrom.replaceFirst(fromArray + "/0", fromArray + "/" + i));
							else
								jpop.setFrom(nestedFrom.replaceFirst(Pattern.quote(fromArray + "/*"), fromArray + "/" + i));
							String nestedPath = jpop.getPath();
							if(nestedPath.startsWith(pathArray + "/0"))
								jpop.setPath(nestedPath.replaceFirst(pathArray + "/0", pathArray + "/" + (i - skipCount)));
							else
								jpop.setPath(nestedPath.replaceFirst(Pattern.quote(pathArray + "/*"), pathArray + "/" + (i - skipCount)));
							jnTarget = apply(jpop, jnTarget, nestedOpMap);
							jpop.setFrom(nestedFrom);
							jpop.setPath(nestedPath);
						}
					}
				}//-------------------------------------
			}
		} else if (fromArray == null && pathArray == null) {// patch single element
			String jPatchMove = COPY.replace("#src", from);
			jPatchMove = jPatchMove.replace("#dest", path);
			jnTarget = JsonPatch.apply(om.readTree(jPatchMove), jnTarget);
		} else if (pathArray != null && fromArray == null) {
			JsonNode jnPathArray = jnTarget.at(pathArray);
			int size = jnPathArray.size();
			for (int i = 0; i < size; i++) {
				String fromXP = from;
				String pathXP = path.replace("/*", "/" + i);
				if (!from.endsWith("/*")) {
					String cond = condition;
					if (condition.contains("#{value}")) {
						cond = condition.replace("#{value}", "#{" + fromXP + "}");
					}
					boolean skipMapping = false;
					if (cond != null && cond.trim().length() > 0)
						skipMapping = !CommonUtils.evaluateCondition(cond, jnTarget, engine);
					if (skipMapping) {
						// skipCount++;
						continue;
					}
					if (function != null && function.trim().length() > 0) {
						String json = jnTarget.at(fromXP).toPrettyString();
						String jsonValue = (String) engine.eval("JSON.stringify(enrich({\"object\":" + json + "}));");
						JsonNode jnJs = om.readTree(jsonValue).at("/object");
						String jPatch = null;
						if (path.endsWith("/*"))
							jPatch = ADD.replace("#dest", pathArray + "/" + i);
						else
							jPatch = ADD.replace("#dest", pathXP);
						jPatch = jPatch.replace("#value", jnJs.toPrettyString());
						jnTarget = JsonPatch.apply(om.readTree(jPatch), jnTarget);
						// skipCount++;
						if(name!=null) {
							List<JPOP> list=nestedOpMap.get(name);
							if(list!=null) {
								for (JPOP jpop : list) {
									String nestedFrom = jpop.getFrom();
									if(nestedFrom.startsWith(fromArray + "/0"))
										jpop.setFrom(nestedFrom.replaceFirst(fromArray + "/0", fromArray + "/" + i));
									else
										jpop.setFrom(nestedFrom.replaceFirst(fromArray + "/*", fromArray + "/" + i));
									//String nestedPath = jpop.getPath();
									//jpop.setPath(nestedPath.replaceFirst(pathArray + "/0", pathArray + "/" + (i - skipCount)).replaceFirst(pathArray + "/*", pathArray + "/" + (i - skipCount)));
									jnTarget = apply(jpop, jnTarget, nestedOpMap);
									jpop.setFrom(nestedFrom);
									//jpop.setPath(nestedPath);
								}
							}
						}//-------------------------------------
						continue;
					}
				}
				String jPatch = COPY.replace("#src", from);
				jPatch = jPatch.replace("#dest", path.replace("/*", "/" + i));
				jnTarget = JsonPatch.apply(om.readTree(jPatch), jnTarget);
				if(name!=null) {
					List<JPOP> list=nestedOpMap.get(name);
					if(list!=null) {
						for (JPOP jpop : list) {
							String nestedFrom = jpop.getFrom();
							if(nestedFrom.startsWith(fromArray + "/0"))
								jpop.setFrom(nestedFrom.replaceFirst(fromArray + "/0", fromArray + "/" + i));
							else
								jpop.setFrom(nestedFrom.replaceFirst(fromArray + "/*", fromArray + "/" + i));
							//String nestedPath = jpop.getPath();
							//jpop.setPath(nestedPath.replaceFirst(pathArray + "/0", pathArray + "/" + (i - skipCount)).replaceFirst(pathArray + "/*", pathArray + "/" + (i - skipCount)));
							jnTarget = apply(jpop, jnTarget, nestedOpMap);
							jpop.setFrom(nestedFrom);
							//jpop.setPath(nestedPath);
						}
					}
				}//-------------------------------------
			}
		}
		return jnTarget;
	}
}
