package com.wwj.weiboClient.workqueue.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoingAndProcess;

/**
 * ��PullFile�����ڴӷ���������ļ�������ʵ��DoingAndProcess�ӿ�,����doingProcess�������������
 * 
 * @author wwj
 * 
 */
public class PullFile implements DoingAndProcess, Const {

	public PullFile() {
		File file = new File(PATH_FILE_CACHE);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	@Override
	public void doingProcess(List list) throws Exception {
		for (Object obj : list) {
			String url = String.valueOf(obj);
			try {
				HttpURLConnection connection = (HttpURLConnection) (new URL(url)
						.openConnection());
				connection.setDoInput(true);
				connection.setUseCaches(false);
				InputStream is = connection.getInputStream();
				FileOutputStream fos = new FileOutputStream(PATH_FILE_CACHE
						+ "/" + url.hashCode());
				// ��is�е���������fos��
				Tools.dataTransfer(is, fos);
				connection.disconnect();
				fos.close();
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public void doingProcess(String url) throws Exception {
		List<String> list = new ArrayList<String>();
		list.add(url);
		doingProcess(list);
	}

}
