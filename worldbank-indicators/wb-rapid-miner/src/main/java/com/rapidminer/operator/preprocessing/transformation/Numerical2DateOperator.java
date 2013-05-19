/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.preprocessing.transformation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import syndeticlogic.wb;
import clojure.lang.PersistentVector;

/**
 * This is the Numerical2Date tutorial operator.
 * 
 * @author Sebastian Land
 */
public class Numerical2DateOperator extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	/**
	 * Constructor
	 */
	public Numerical2DateOperator(OperatorDescription description) {
		super(description);
		System.out.println("Numerical2DateOperator CREATED");
		//if(true) throw new RuntimeException("JAMES ##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\n##################################################################\nI GOT CALLED!!!");
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "relative time" }, Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				AttributeMetaData timeAMD = metaData.getAttributeByName("relative time");
				if (timeAMD != null) {
					timeAMD.setType(Ontology.DATE_TIME);
					timeAMD.setName("date(" + timeAMD.getName() + ")");
					timeAMD.setValueSetRelation(SetRelation.UNKNOWN);
				}
				return metaData;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
		System.out.println("DOWORK CALLED");
		ExampleSet exampleSet = exampleSetInput.getData();
		Attributes attributes = exampleSet.getAttributes();
		Attribute sourceAttribute = attributes.get("relative time");
		String newName = "date(" + sourceAttribute.getName() + ")";
		Attribute targetAttribute = AttributeFactory.createAttribute(newName, Ontology.DATE_TIME);
		targetAttribute.setTableIndex(sourceAttribute.getTableIndex());
		attributes.addRegular(targetAttribute);
		attributes.remove(sourceAttribute);

		for (Example example : exampleSet) {
			double timeStampValue = example.getValue(targetAttribute);
			example.setValue(targetAttribute, timeStampValue * 1000);
		}

		exampleSetOutput.deliver(exampleSet);
	}
	
	public static void main(String[] args) {
		try {
	        System.out.println("Indicator IDs " + wb.get_indicator_ids().toString());
		} catch (Throwable e) {
			//System.out.println("\nCatching exception e" + e);
			//System.out.println("");
			e.printStackTrace();
		}
	        //System.out.println("(binomial 10042, 111): " + tiny.binomial(10042, 111));
	    }
}
