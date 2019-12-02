package xbrlcore.playground;

import java.io.File;

import xbrlcore.instance.Instance;
import xbrlcore.instance.InstanceFactory;
import xbrlcore.instance.InstanceOutputter;

public class PlaygroundInstanceCreation {

	private static final String BASE_FOLDER = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/instance/PPTahunan/GeneralFinance/";
	private static final String TUPLE_FILE = BASE_FOLDER + "000000-2019-12-31-003020600.xml";
	private static final String DIMENSIONAL_FILE = BASE_FOLDER + "000000-2019-12-31-110020600.xml";
	private static final String FIXED_FILE = BASE_FOLDER + "000000-2019-12-31-000020600.xml";
	
	private static final String MULTI_DIMENSIONAL_FILE = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/instance/PP/GeneralFinance/000000-2019-11-30-531020300.xml";
	
	private static final String OUTPUT_FOLDER = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/instance/PP/GeneralFinance";
	private static final String MULTI_DIMENSIONAL_OUTPUT = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/instance/PP/GeneralFinance";
	
		
	public static void main(String[] args) throws Exception {
		File instanceFile = null;
		Instance instance = null;
		InstanceOutputter io = null;
		
		/**
		instanceFile = new File(FIXED_FILE);
		instance = InstanceFactory.get().createInstance(instanceFile);
		io = new InstanceOutputter(instance);
		io.saveAsFile(new File(OUTPUT_FOLDER + File.separator + "fixed.xml"));
		*/
		
		instanceFile = new File(TUPLE_FILE);
		instance = InstanceFactory.get().createInstance(instanceFile);
		io = new InstanceOutputter(instance);
		io.saveAsFile(new File(OUTPUT_FOLDER + File.separator + "tuple.xml"));
		
		instanceFile = new File(MULTI_DIMENSIONAL_FILE);
		instance = InstanceFactory.get().createInstance(instanceFile);
		io = new InstanceOutputter(instance);
		io.saveAsFile(new File(OUTPUT_FOLDER + File.separator + "dimension.xml"));
	}
	
}
