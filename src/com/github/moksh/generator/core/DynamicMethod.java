package com.github.moksh.generator.core;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonPatchBuilder;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.moksh.generator.core.Function.Param;

import lombok.Data;

public class DynamicMethod {
	public String concat(String instr1, String instr2) {
		String outStr = instr1 + instr2;
		// System.out.println(outStr);
		return outStr;
	}

	public static void main(String[] args) throws Exception {
		String sig = getSignature("com.logilabs.pub.string.Concat", 0);
		System.out.println(sig);
		JsonObject payload = Json.createReader(new StringReader(
				"{\"id\":\"0\",\"com.logilabs.pub.string.Concat\":{\"request\":{\"instr1\": \"hello \",\"instr2\": \"world\"},\"response\":\"\"}}"))
				.readObject();
		String result = call("com.logilabs.pub.string.Concat", payload);
		System.out.println(result);
	}

	public static String getSignature(String functionClassName, int id) throws Exception {
		String methodName="execute";
		Class methodClass = Class.forName(functionClassName);
		Method methods[] = methodClass.getDeclaredMethods();
		String sigJson = "";
		Type returnType;
		List<Param> params = new ArrayList<Param>();
		for (Method method : methods) {
			String name = method.getName();
			if (methodName.equals(name)) {
				returnType = method.getReturnType();
				Parameter parameters[] = method.getParameters();
				Function func = new Function(returnType, methodName, params);
				for (Parameter parameter : parameters) {
					Type type = parameter.getType();
					params.add(func.new Param(type, parameter.getName()));
				}
				sigJson = func.toString();
				break;
			}
		}
		sigJson = "{\"id\":\"" + id + "\",\"" + functionClassName + "\":" + sigJson + "}";
		return sigJson;
	}

	public static String call(String functionClassName, JsonObject payload) throws Exception {
		String jsonResult = "";
		String methodName="execute";
		Class methodClass = Class.forName(functionClassName);
		ObjectMapper om = new ObjectMapper();
		Method methods[] = methodClass.getDeclaredMethods();
		Type returnType;
		List<Object> params = new ArrayList<Object>();
		for (Method method : methods) {
			String name = method.getName();
			if (methodName.equals(name)) {
				returnType = method.getReturnType();
				Parameter parameters[] = method.getParameters();
				for (Parameter parameter : parameters) {
					Type type = parameter.getType();
					JsonValue jvParam = payload.getValue("/" + functionClassName + "/request/" + parameter.getName());
					Class LoadedClass = type.getClass().forName(type.getTypeName());
					Object inst = om.readValue(jvParam.toString(), LoadedClass);
					params.add(inst);
				}
				Object result = method.invoke(methodClass.newInstance(), params.toArray());
				if (returnType.toString().contains("java.lang.String"))
					jsonResult = result.toString();
				else
					jsonResult = om.writeValueAsString(result);
				JsonPatchBuilder builder = Json.createPatchBuilder();
				payload = builder.add("/" + functionClassName + "/response", jsonResult).build().apply(payload);
				jsonResult = payload.toString();
				break;
			}
		}
		return jsonResult;
	}
}

class Function {
	public String name;
	public Type returnType;
	public List<Param> params;
	final ObjectMapper om = new ObjectMapper();

	@Override
	public String toString() {
		String data = "{\"request\":{";
		if (params != null && params.size() > 0) {
			for (Param prm : params) {
				Type type = prm.getType();
				if (((Class<?>) type).isPrimitive())
					data += "\"" + prm.getName() + "\": \"\",";
				else
					data += "\"" + prm.getName() + "\": " + prm.getJsonValue() + ",";
			}
			data += ",";
			data = data.replace(",,", "");
		}
		data += "},\"response\":";
		Class LoadedClass;
		try {
			LoadedClass = returnType.getClass().forName(returnType.getTypeName());
			Object inst = LoadedClass.newInstance();
			data += om.writeValueAsString(inst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(name);

		data += "}";
		return data;
	}

	public Function(Type returnType, String name, List<Param> params) {
		this.name = name;
		this.returnType = returnType;
		this.params = params;
	}

	public void addParam(Param param) {
		params.add(param);
	}

	public Param getParam(String name) {
		return params.stream().filter(prmm -> name.equals(prmm.getName())).findAny().orElse(null);
	}

	@Data
	class Param {
		public String name;
		public Type type;

		public Param(Type type, String name) {
			this.name = name;
			this.type = type;
		}

		public String getJsonValue() {
			String data = "";
			try {
				Class LoadedClass = type.getClass().forName(type.getTypeName());
				// System.out.println(name);
				Object inst = LoadedClass.newInstance();
				data += om.writeValueAsString(inst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return data;
		}
	}

}
