/***
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.ceenic.sidenavigation.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ceenic.sidenavigation.R;
import com.ceenic.sidenavigation.SideNavigationLayout;
import com.ceenic.sidenavigation.SideNavigationLayout.SideNavigationListener;

public class MainActivity extends Activity implements SideNavigationListener {

    private SideNavigationLayout mSideNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSideNavigationView = (SideNavigationLayout) findViewById(R.id.side_nav);
        mSideNavigationView.setNavigationListener(this);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new TestAdapter());
        ListView listView2 = (ListView) findViewById(R.id.list2);
        listView2.setAdapter(new TestAdapter2());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_toggle_nav) {
            if (mSideNavigationView.isShowingNavigationView()) {
                mSideNavigationView.showContentView();
            } else {
                mSideNavigationView.showNavigationView();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class TestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(parent.getContext());
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        LayoutParams.MATCH_PARENT, 100);
                convertView.setLayoutParams(params);
            }
            TextView textView = (TextView) convertView;
            textView.setText("TExt");
            return convertView;
        }

    }

    private class TestAdapter2 extends BaseAdapter {

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(parent.getContext());
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        LayoutParams.MATCH_PARENT, 400);
                convertView.setLayoutParams(params);
            }
            TextView textView = (TextView) convertView;
            textView.setBackgroundColor(Color.BLACK);
            textView.setTextColor(Color.WHITE);
            textView.setText("This is your content");
            return convertView;
        }

    }

    @Override
    public void onShowNavigationView(SideNavigationLayout view) {
        Log.d("TEST", "navigation");
        // getActionBar().setTitle("Navigation");
    }

    @Override
    public void onShowContentView(SideNavigationLayout view) {
        Log.d("TEST", "content");
        // getActionBar().setTitle("Content");

    }
}
