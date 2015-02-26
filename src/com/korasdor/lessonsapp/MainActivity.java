package com.korasdor.lessonsapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity{
	
	ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		listView = (ListView) findViewById(com.korasdor.lessonsapp.R.id.listView);
		
		String[] values = new String[]{"BaseCameraActivity","CameraActivity","Lesson3","Lesson4"};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, android.R.id.text1, values );
		
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
//				int itemPosition = position;
//				
				String className = ".lessons." + ((String) listView.getItemAtPosition(position));
				
				String pkg = "com.korasdor.lessonsapp";
				
				Intent intent=new Intent();
				intent.setComponent(new ComponentName(pkg, pkg + className));
				startActivity(intent);
			}
		});
	}
}
