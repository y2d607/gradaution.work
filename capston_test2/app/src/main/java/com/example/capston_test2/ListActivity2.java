package com.example.capston_test2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.util.Log;

public class ListActivity2 extends Activity {

    private final ArrayList<PostItem> listItems = new ArrayList<>();
    private ArrayAdapter<PostItem> adapter;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListView listView = findViewById(R.id.listView);
        textView = findViewById(R.id.TextView);
        adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, listItems);
        listView.setAdapter(adapter);

        String jspUrl = "";
        // 인텐트로부터 buttonText를 가져옵니다.
        String buttonText = getIntent().getStringExtra("buttonText");

        // 여기서 buttonText 값을 기반으로 jspUrl을 설정합니다.
        if ("파일제출".equals(buttonText)) {textView.setText(buttonText);
            jspUrl = "http://www.shinhan-software.co.kr/mobile_hw.jsp";
        }else {textView.setText(buttonText);
            // 기본 URL 또는 다른 버튼 텍스트에 대한 처리
            jspUrl = "";
        }


        new RetrieveFeedTask().execute(jspUrl);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PostItem selectedItem = listItems.get(position);

                // BoardActivity로 화면 전환. 이때, post_id도 함께 전달합니다.
                Intent intent = new Intent(ListActivity2.this, BoardActivity2.class);
                intent.putExtra("post_id", selectedItem.getPostId());
                startActivity(intent);

               /* // DownloadActivity에게 post_id 전송
                Intent broadcastIntent = new Intent("com.example.listtest2.SEND_POST_ID");
                broadcastIntent.putExtra("post_id", selectedItem.getPostId());
                sendBroadcast(broadcastIntent);*/
            }
        });
    }

    private class PostItem {
        private final String displayText;
        private final String postId;

        public PostItem(String displayText, String postId) {
            this.displayText = displayText;
            this.postId = postId;
        }

        public String getDisplayText() {
            return displayText;
        }

        public String getPostId() {
            return postId;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    private class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // 서버 오류 로깅 및 오류 문자열 반환
                    Log.e("RetrieveFeedTask", "HTTP error code: " + responseCode);
                    return "Server returned HTTP error code: " + responseCode;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n'); // 개행 문자 추가
                }
                reader.close();
                return stringBuilder.toString();

            } catch (Exception e) {
                Log.e("RetrieveFeedTask", "Error ", e);
                return null; // null 반환시 처리 로직에 따라 사용자에게 알림
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.e("MyApp", "Result from server is null.");
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // 'post_id' 키의 존재 여부를 확인하고, 없을 경우 기본값 처리
                    // optString 메서드는 지정된 키에 대한 값을 반환하거나, 해당 키가 없을 경우 지정된 기본값을 반환합니다.
                    String postId = jsonObject.optString("post_id", "default");


                    String ctime = jsonObject.getString("ctime");
                    String dateOnly = ctime.substring(0, 10);  // "yyyy-MM-dd" 만 추출

                    String displayText = " 제   목 : "+ jsonObject.getString("title") + " \n " +
                            "작성자 : "+  jsonObject.getString("author") + " \n " +"\n"+"                                                               "+
                            dateOnly;//jsonObject.getString("ctime");
                    //+ "\n" + "파일위치 : " + jsonObject.getString("filelocation") + " \n " + "post_id : " + postId;

                    PostItem item = new PostItem(displayText, postId); // postId 사용
                    listItems.add(item);
                }
                adapter.notifyDataSetChanged();  // 리스트 뷰를 갱신합니다.
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MyApp", "Error parsing JSON", e);
            }
        }
    }



}