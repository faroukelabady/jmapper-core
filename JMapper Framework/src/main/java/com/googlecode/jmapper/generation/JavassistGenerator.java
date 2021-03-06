/**
 * Copyright (C) 2012 - 2016 Alessandro Vurro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.jmapper.generation;

import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

import com.googlecode.jmapper.IMapper;
import com.googlecode.jmapper.config.Error;
import com.googlecode.jmapper.generation.beans.Constructor;
import com.googlecode.jmapper.generation.beans.Method;
import static com.googlecode.jmapper.util.GeneralUtility.isNull;
/**
 * Javassist implementation.
 * 
 * @author Alessandro Vurro
 *
 */
public class JavassistGenerator implements ICodeGenerator {
	
	static{
		// ClassPool initialization
		ClassPool.getDefault().insertClassPath(new ClassClassPath(IMapper.class));
	}
	
	public Class<?> generate(String clazzName, List<Constructor> constructors, List<Method> methods) throws Throwable {
		
		CtClass cc = null;
		
		try{
			ClassPool cp = ClassPool.getDefault();
			// create the class
			cc = cp.makeClass(clazzName);
			
			// adds the interface
			cc.addInterface(cp.get(IMapper.class.getName()));
			
			// adds constructor
			for (Constructor constructor : constructors) {
				// create constructor
				CtConstructor ctConstructor = new CtConstructor(toCtClass(constructor.getParameters()), cc);
				// set body constructor
				ctConstructor.setBody(constructor.getBody());
				// add constructor to CtClass
				cc.addConstructor(ctConstructor);	
			}
			
			// adds methods
			for (Method method : methods) {
				try{// create method
					CtMethod ctMethod = new CtMethod(toCtClass(method.getReturnType())[0],method.getName(), toCtClass(method.getParameters()), cc);
					// set body method
					ctMethod.setBody(method.getBody());
					// add method to CtClass
					cc.addMethod(ctMethod); }
				catch (CannotCompileException e) { 
					Error.bodyContainsIllegalCode(method,e); } 
			}
			
			Class<?> generetedClass = cc.toClass();
			return generetedClass;
			
		}catch (NotFoundException e) { 
			Error.notFoundException(e); 
			
		}finally{
			if(!isNull(cc)) 
				cc.defrost();
		}
		
		return null;
	}
	
	/**
	 * This method transforms classes in CtClass[]
	 * @param classes
	 * @return CtClass[] version of classes parameter
	 * @throws Exception in case of not found class
	 */
	private static CtClass[] toCtClass(Class<?>... classes) throws Exception{
		ClassPool cp = ClassPool.getDefault();
		CtClass[] parameters = new CtClass[classes.length];
		for(int i=0;i<classes.length;i++)
			parameters[i]=cp.get(classes[i].getName());
		
		return parameters;
	}
}
