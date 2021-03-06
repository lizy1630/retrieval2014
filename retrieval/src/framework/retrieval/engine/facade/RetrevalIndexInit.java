/**
 * Copyright 2010 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package framework.retrieval.engine.facade;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import framework.base.snoic.base.util.StringClass;
import framework.base.snoic.base.util.file.FileHelper;
import framework.base.snoic.base.util.file.FileSizeHelper;
import framework.base.snoic.base.util.file.FilenameFilterHelper;
import framework.retrieval.engine.analyzer.IRAnalyzerFactory;
import framework.retrieval.engine.common.RetrievalUtil;
import framework.retrieval.engine.context.LuceneProperties;
import framework.retrieval.engine.facade.impl.RIndexOperatorFacade;



/**
 * 初始化索引
 * @author 
 *
 */
public class RetrevalIndexInit {
	private Log log=RetrievalUtil.getLog(this.getClass());

	private static Object object=new Object();
	private volatile static boolean loadFlag=false;
	private FileHelper fileHelper = new FileHelper();
	private FileSizeHelper fileSizeHelper = new FileSizeHelper();
	
	public final static String FILE_NUMERIC_JOIN = "_";
	
	public final static int FILE_NUMERIC_START = 1;
	
	public RetrevalIndexInit(){
		
	}

	/**
	 * 初始化
	 */
	public void init(IRAnalyzerFactory analyzerFactory,
			LuceneProperties luceneProperties,
			String[] indexPathTypes) {
		synchronized(object){
			if(!loadFlag){
				loadFlag=true;
				if(indexPathTypes!=null){
					int length=indexPathTypes.length;
					for(int i=0;i<length;i++){
						String indexPathType=indexPathTypes[i].toUpperCase();
						try{
							IRIndexOperatorFacade indexOperatorFacade=new RIndexOperatorFacade(analyzerFactory,luceneProperties,indexPathType);
							indexOperatorFacade.createIndex();
						}catch(Exception e){
							RetrievalUtil.errorLog(log, e);
						}
					}
				}
			}
		}
	}
	
	public String[] initset(IRAnalyzerFactory analyzerFactory,LuceneProperties luceneProperties,String[] indexPathTypes,long fileMaxsize) {
		synchronized(object){
			if(indexPathTypes!=null){
				int length=indexPathTypes.length;
				for(int i=0;i<length;i++){
					String indexPathType=indexPathTypes[i].toUpperCase();
					String indexPath = StringClass.getFormatPath(luceneProperties.getIndexBasePath()+"/"+String.valueOf(indexPathType+FILE_NUMERIC_JOIN+FILE_NUMERIC_START).toUpperCase());
					if(fileHelper.isExists(indexPath)){
						int lastPosition = indexPathType.lastIndexOf("/");
						String indexfile = indexPathType.substring(lastPosition>0?lastPosition+1:0);
						String[] fn = {indexfile};
						FilenameFilterHelper filter = new FilenameFilterHelper(fn);
						File[] fs = new File(indexPath).getParentFile().listFiles(filter);
						Integer[] numerics = new Integer[fs.length];
						for(int j = 0;j<fs.length;j++){
							String numeric = fs[j].getName().replace(indexfile, "").substring(1);
							if(StringClass.isNumeric(numeric))
								numerics[j] = Integer.parseInt(numeric);
						}
						Arrays.sort(numerics,Collections.reverseOrder());
						int max = numerics[0];
						
						long fileSize = fileSizeHelper.getFilesSizes(StringClass.getFormatPath(luceneProperties.getIndexBasePath()+"/"+indexPathType+FILE_NUMERIC_JOIN+max));
						if(fileSize>fileMaxsize){
							indexPathTypes[i] = indexPathType+FILE_NUMERIC_JOIN+(max+FILE_NUMERIC_START);
							createInitIndex(analyzerFactory,luceneProperties,indexPathTypes[i]);
						}else{
							indexPathTypes[i] = indexPathType+FILE_NUMERIC_JOIN+max;
						}
					}else{
						indexPathTypes[i] = indexPathType+FILE_NUMERIC_JOIN+FILE_NUMERIC_START;
						createInitIndex(analyzerFactory,luceneProperties,indexPathTypes[i]);
					}
				}
			}
		}
		return indexPathTypes;
	}
	
	public void createInitIndex(IRAnalyzerFactory analyzerFactory,LuceneProperties luceneProperties,String indexPathType){
		try{
			IRIndexOperatorFacade indexOperatorFacade=new RIndexOperatorFacade(analyzerFactory,luceneProperties,indexPathType);
			indexOperatorFacade.createIndex();
		}catch(Exception e){
			RetrievalUtil.errorLog(log, e);
		}
	}
	
	public String[] getLocalIndexPathTypes(LuceneProperties luceneProperties,String[] indexPathTypes){
		List<String> indexPathList = null;
		if(indexPathTypes!=null){
			int length=indexPathTypes.length;
			if(length>0){
				indexPathList = new ArrayList<String>();
				for(int i=0;i<length;i++){
					String indexPathType = RetrievalUtil.getIndexPathTypeFormat(indexPathTypes[i]);
					String indexPath = StringClass.getFormatPath(luceneProperties.getIndexBasePath()+"/"+String.valueOf(indexPathType+FILE_NUMERIC_JOIN+FILE_NUMERIC_START).toUpperCase());
					if(fileHelper.isExists(indexPath)){
						int lastPosition = indexPathType.lastIndexOf("/");
						String indexfile = indexPathType.substring(lastPosition>0?lastPosition+1:0);
						String[] fn = {indexfile};
						FilenameFilterHelper filter = new FilenameFilterHelper(fn);
						File[] fs = new File(indexPath).getParentFile().listFiles(filter);
						for(File f : fs){
							String numeric = f.getName().replace(indexfile, "").substring(1);
							if(StringClass.isNumeric(numeric))
								indexPathList.add(indexPathType+FILE_NUMERIC_JOIN+numeric);
						}
					}
				}
			}
		}
		return indexPathList.toArray(new String[indexPathList.size()]);
	}
	
}
