package org.pentaho.di.trans.steps.spatialanalysis;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.spatialanalysis.Messages;
import org.w3c.dom.Node;

import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * @author jmathieu
 * @since nov-2010
 */
public class SpatialAnalysisMeta extends BaseStepMeta implements StepMetaInterface{
	private String 		referenceStepName;
	private StepMeta 	referenceStepMeta;

	private String 		compareStepName;  
	private StepMeta 	compareStepMeta;

    private String   	referenceField;
    private String   	compareField;    
    private String 		distField;    
    private String 		resultfieldName;  
    
    private boolean 	compressFiles;
    private boolean 	oneRow;

    public static final String UNION = Messages.getString("SpatialAnalysisMeta.AnalysisType.UNION");
    public static final String INTERSECTION = Messages.getString("SpatialAnalysisMeta.AnalysisType.INTERSECTION");
    public static final String BUFFER = Messages.getString("SpatialAnalysisMeta.AnalysisType.BUFFER");
    public static final String SYM_DIFF = Messages.getString("SpatialAnalysisMeta.AnalysisType.SYM_DIFF");
    public static final String INT_POINT = Messages.getString("SpatialAnalysisMeta.AnalysisType.INT_POINT");
    public static final String ENVELOPE = Messages.getString("SpatialAnalysisMeta.AnalysisType.ENVELOPE");
    public static final String CENTROID = Messages.getString("SpatialAnalysisMeta.AnalysisType.CENTROID");
    public static final String BOUNDARY = Messages.getString("SpatialAnalysisMeta.AnalysisType.BOUNDARY");
    public static final String DIFFERENCE = Messages.getString("SpatialAnalysisMeta.AnalysisType.DIFFERENCE");
    public static final String CONVEX_HULL = Messages.getString("SpatialAnalysisMeta.AnalysisType.CONVEX_HULL");
    public static final String REVERSE = Messages.getString("SpatialAnalysisMeta.AnalysisType.REVERSE");
    public static final String SPLITGC = Messages.getString("SpatialAnalysisMeta.AnalysisType.SPLITGC");
    
    public static String spatialAnalysisTypeCodes[] = {UNION, INTERSECTION, BUFFER, SYM_DIFF, INT_POINT, ENVELOPE, CENTROID, BOUNDARY, DIFFERENCE, CONVEX_HULL, REVERSE, SPLITGC};

    public static final String[] bufferSides = {Messages.getString("SpatialAnalysisMeta.Side.Left"), Messages.getString("SpatialAnalysisMeta.Side.Right"), Messages.getString("SpatialAnalysisMeta.Side.Both")};
    
    public static final String[] bufferCaps = {Messages.getString("SpatialAnalysisMeta.Cap.Flat"), Messages.getString("SpatialAnalysisMeta.Cap.Round"), Messages.getString("SpatialAnalysisMeta.Cap.Square")};
   
    public static final String[] bufferJoins = {Messages.getString("SpatialAnalysisMeta.Join.Bevel"), Messages.getString("SpatialAnalysisMeta.Join.Mitre"), Messages.getString("SpatialAnalysisMeta.Join.Round")};
	
    private String spatialAnalysisType;
		
    private String bufferCap;
    private String bufferSide;
    private String bufferJoin;
    
	public void setSpatialAnalysisType(int i) {
		spatialAnalysisType = spatialAnalysisTypeCodes[i];
	}

	public boolean isAlgoDual(){
		boolean algoDual = false;
        switch (getSpatialAnalysisByDesc()){
            case 0: 
            	algoDual = true;
            	break;
            case 1:
            	algoDual = true;
            	break;
            case 3:
            	algoDual = true;
            	break;
            case 8:
            	algoDual = true;
            	break;     
            default: 
            	break;
        }
        return algoDual;
	}
	
	public int getSpatialAnalysisByDesc() {
		int retval = 0;
		if (spatialAnalysisType != null){
			for(int i = 0; i < spatialAnalysisTypeCodes.length; i++){
				if(spatialAnalysisType.equals(spatialAnalysisTypeCodes[i]))
					return i;
			}
		}
		return retval;
	}

	public String getSpatialAnalysisType() {
		return spatialAnalysisType;
	}

    public String getReferenceField(){
        return referenceField;
    }
    
    public void setDistField(String d) {
		this.distField = d;
	}
    
    public String getDistField() {
		return distField;
	}

    public void setReferenceField(String refField){
        this.referenceField = refField;
    }
    
    public String getCompareField(){
        return compareField;
    }

    public void setCompareField(String compField){
        this.compareField = compField;
    }

	public String getResultFieldName() {
		return resultfieldName;
	}

	public void setResultFieldName(String resultfieldName) {
		this.resultfieldName = resultfieldName;
	}
	
	public boolean getCompressFiles(){
        return compressFiles;
    }
	
	public void setCompressFiles(boolean compressFiles){
        this.compressFiles = compressFiles;
    }
	
	public boolean getOneRow(){
        return oneRow;
    }
	
	public void setOneRow(boolean oneRow){
        this.oneRow = oneRow;
    }
	
	public void setSide(String side) {
		this.bufferSide = side;
	}
	 
	public void setCap(String cap) {
		this.bufferCap = cap;
	}
	
	public void setJoin(String join) {
		this.bufferJoin = join;
	}
	
	public int getCapAsInt(){
		int cap = -1;
        if(bufferCap.equals(bufferCaps[0]))
        	cap = BufferParameters.CAP_FLAT;
        if(bufferCap.equals(bufferCaps[1]))
        	cap = BufferParameters.CAP_ROUND;
        if(bufferCap.equals(bufferCaps[2]))
        	cap = BufferParameters.CAP_SQUARE;
        return cap;
    }
	
	public String getSide(){
        return bufferSide;
    }
	
	public String getCap(){
        return bufferCap;
    }
	
	public String getJoin(){
        return bufferJoin;
    }
	
	public int getJoinAsInt(){
		int join = -1;
        if(bufferJoin.equals(bufferJoins[0]))
        	join = BufferParameters.JOIN_BEVEL;
        if(bufferJoin.equals(bufferJoins[1]))
        	join = BufferParameters.JOIN_MITRE;
        if(bufferJoin.equals(bufferJoins[2]))
        	join = BufferParameters.JOIN_ROUND;
        return join;
    }
	
    public SpatialAnalysisMeta(){
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException{
		readData(stepnode);
	}

    public String getCompareStepName(){
		return (compareStepMeta!=null && compareStepMeta.getName()!=null && compareStepMeta.getName().length()>0)?compareStepMeta.getName():null;
	}
 
    public String getReferenceStepName(){
		return (referenceStepMeta!=null && referenceStepMeta.getName()!=null && referenceStepMeta.getName().length()>0)?referenceStepMeta.getName():null;
    }
    
    public void setCompareStepName(String sendFalseStepname){
        this.compareStepName = sendFalseStepname;
    }
    
    public void setReferenceStepName(String sendTrueStepname){
        this.referenceStepName = sendTrueStepname;
    }
    
    public StepMeta getCompareStepMeta(){
        return compareStepMeta;
    }
    
    public StepMeta getReferenceStepMeta(){
        return referenceStepMeta;
    }
    
    public void setCompareStepMeta(StepMeta sendFalseStep){
        this.compareStepMeta = sendFalseStep;
    }
	
    public void setReferenceStepMeta(StepMeta sendTrueStep){
        this.referenceStepMeta = sendTrueStep;
    }

	public Object clone(){
        return (SpatialAnalysisMeta)super.clone();
	}
	
	public String getXML(){
        StringBuffer retval = new StringBuffer();      
        retval.append("	   ").append(XMLHandler.addTagValue("spatialAnalysisType", spatialAnalysisType));
        retval.append("	   ").append(XMLHandler.addTagValue("resultfieldName", resultfieldName));
        retval.append("	   ").append(XMLHandler.addTagValue("compress", compressFiles));
        retval.append("	   ").append(XMLHandler.addTagValue("oneRow", oneRow));
        retval.append(XMLHandler.addTagValue("referenceField", referenceField));
        retval.append(XMLHandler.addTagValue("compareField", compareField));
        retval.append(XMLHandler.addTagValue("distField", distField));
		retval.append(XMLHandler.addTagValue("reference", getReferenceStepName()));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("cap", getCap()));	
		retval.append(XMLHandler.addTagValue("side", getSide()));	
		retval.append(XMLHandler.addTagValue("join", getJoin()));	
		retval.append(XMLHandler.addTagValue("compare", getCompareStepName()));		 //$NON-NLS-1$
		retval.append("    <compare>"+Const.CR); //$NON-NLS-1$				
		retval.append("    </compare>"+Const.CR); //$NON-NLS-1$
		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException{
		try{ 			
			spatialAnalysisType = XMLHandler.getTagValue(stepnode, "spatialAnalysisType");
			resultfieldName = XMLHandler.getTagValue(stepnode, "resultfieldName");      
            referenceField = XMLHandler.getTagValue(stepnode, "referenceField"); //$NON-NLS-1$
            compareField = XMLHandler.getTagValue(stepnode, "compareField"); //$NON-NLS-1$           
			compareStepName = XMLHandler.getTagValue(stepnode, "compare"); //$NON-NLS-1$
			referenceStepName = XMLHandler.getTagValue(stepnode, "reference"); //$NON-NLS-1$	
			bufferCap = XMLHandler.getTagValue(stepnode, "cap"); //$NON-NLS-1$
			bufferJoin = XMLHandler.getTagValue(stepnode, "join"); //$NON-NLS-1$
			bufferSide = XMLHandler.getTagValue(stepnode, "side"); //$NON-NLS-1$		
			compressFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "compress"));
			oneRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "oneRow"));
			distField = XMLHandler.getTagValue(stepnode, "distField"); //$NON-NLS-1$
		}catch(Exception e){
			throw new KettleXMLException(Messages.getString("SpatialAnalysisMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault(){
		resultfieldName = null;
		distField="0";
		spatialAnalysisType = spatialAnalysisTypeCodes[0];
		compressFiles = false;
		oneRow = false;
	}
    
    public String[] getInfoSteps(){
    	String [] infos = null;
        if (referenceStepMeta!=null)
        	infos = compareStepMeta!=null?new String[] {referenceStepMeta.getName(), compareStepMeta.getName(),}:new String[] {referenceStepMeta.getName()};         
        return infos;
    }

    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps){
        if (infoSteps!=null && infoSteps.length==2){
            referenceStepMeta = infoSteps[0];
            compareStepMeta = infoSteps[1];
        }
    }

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException{
		try{			
			spatialAnalysisType = rep.getStepAttributeString(id_step, "spatialAnalysisType");
			resultfieldName = rep.getStepAttributeString(id_step, "resultfieldName");
			referenceField = rep.getStepAttributeString (id_step, "referenceField");  //$NON-NLS-1$
			compareField = rep.getStepAttributeString (id_step, "compareField");  //$NON-NLS-1$			
			referenceStepName = rep.getStepAttributeString (id_step, "reference");  //$NON-NLS-1$
			compareStepName = rep.getStepAttributeString (id_step, "compare");  //$NON-NLS-1$	
			bufferJoin = rep.getStepAttributeString (id_step, "join");  //$NON-NLS-1$
			bufferSide = rep.getStepAttributeString (id_step, "side");  //$NON-NLS-1$
			bufferCap = rep.getStepAttributeString (id_step, "cap");  //$NON-NLS-1$		
			compressFiles = rep.getStepAttributeBoolean(id_step, "compress");
			oneRow = rep.getStepAttributeBoolean(id_step, "oneRow");
			distField = rep.getStepAttributeString (id_step, "distField");  //$NON-NLS-1$
		}catch(Exception e){
			throw new KettleException(Messages.getString("SpatialAnalysisMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "spatialAnalysisType", spatialAnalysisType);
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldName", resultfieldName);			
			rep.saveStepAttribute(id_transformation, id_step, "distField", distField);
			rep.saveStepAttribute(id_transformation, id_step, "compress", compressFiles);
			rep.saveStepAttribute(id_transformation, id_step, "oneRow", oneRow);
			rep.saveStepAttribute(id_transformation, id_step, "referenceField", referenceField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "compareField", compareField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "reference", getReferenceStepName()); //$NON-NLS-1$			
			rep.saveStepAttribute(id_transformation, id_step, "cap", getCap()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "join", getJoin()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "side", getSide()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "compare", getCompareStepName()); //$NON-NLS-1$
		}catch(Exception e){
			throw new KettleException(Messages.getString("SpatialAnalysisMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps){
		referenceStepMeta  = StepMeta.findStep(steps, referenceStepName);
		compareStepMeta = StepMeta.findStep(steps, compareStepName);
	}

	public boolean chosesTargetSteps(){
	    return false;
	}

	public String[] getTargetSteps(){
	    return null;
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException{                     
    	if (info!=null){
            for (int i=0;i<info.length;i++){
                if (info[i]!=null){              	
                	r.mergeRowMeta(info[i]);    
                }                            
            }
        }
    	
    	// Output field (Geometry)
		if (!Const.isEmpty(resultfieldName)) {
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(resultfieldName),
						ValueMeta.TYPE_GEOMETRY);			
			v.setOrigin(name);
			r.addValueMeta(v);
		}
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info){
		CheckResult cr;
		
		if (getReferenceStepName()!=null)
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("SpatialAnalysisMeta.CheckResult.ReferenceStepOK"), stepinfo);
		else
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.ReferenceStepMissing"), stepinfo);
		remarks.add(cr);
		
		if (Const.isEmpty(referenceField))
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.NoReferenceFieldSpecified"), stepinfo);
		else
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("SpatialAnalysisMeta.CheckResult.ReferenceFieldSpecified"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
		
		int analysisType = getSpatialAnalysisByDesc();
		
		if (analysisType == 2){//check if distance is a double for buffer
			try{
				 Double.parseDouble(getDistField());
			}catch(Exception e){
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.DistFieldMustBeDouble"), stepinfo);
				remarks.add(cr);
			}
		}
		
		//check compare input if transformation requires 2 streams
		if (isAlgoDual()){
			if (getCompareStepName()!=null){
				if(getCompareStepName().equals(getReferenceStepName()))
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.CompareSameAsReference"), stepinfo);
				else
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("SpatialAnalysisMeta.CheckResult.CompareStepOK"), stepinfo);
				remarks.add(cr);
				if (Const.isEmpty(compareField))
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.NoCompareFieldSpecified"), stepinfo);
				else
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("SpatialAnalysisMeta.CheckResult.CompareFieldSpecified"), stepinfo); //$NON-NLS-1$								
				remarks.add(cr);			
			}else{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.CompareStepMissing"), stepinfo);			
				remarks.add(cr);
			}
		}
						
		if (Const.isEmpty(resultfieldName))
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("SpatialAnalysisMeta.CheckResult.NoResultFieldSpecified"), stepinfo);	
		else
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("SpatialAnalysisMeta.CheckResult.ResultFieldSpecified"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);	
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans){
		return new SpatialAnalysis(stepMeta, stepDataInterface, cnr, tr, trans);
	}

    public boolean excludeFromRowLayoutVerification(){
        return true;
    }
    
	public StepDataInterface getStepData(){
		return new SpatialAnalysisData();
	}
}