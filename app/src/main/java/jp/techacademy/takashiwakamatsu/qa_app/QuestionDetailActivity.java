package jp.techacademy.takashiwakamatsu.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements DatabaseReference.CompletionListener {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;
    private DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
    private String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Button btnFavorite;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.d("ever","onChildAddedに入りました");

            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

            if ( mQuestion.getTitle().equals( (String) map.get("title") ))  {
                //Button btnFavorite = (Button) findViewById(R.id.favorite);
                btnFavorite.setText("お気に入りに入っています");
                Log.d("ever", "判定ルーチンに入りました");
            }


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("ever","onChildChanged入りました");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d("ever","onChildRemoved入りました");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d("ever","onChildMoved入りました");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("ever","onCancelled入りました");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        //この質問が、お気に入りに入っているか判定する
        btnFavorite = (Button) findViewById(R.id.favorite);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // ログイン済みのユーザーを収録する
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
               if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

        //UserID+質問IDで、DBの参照を作成
        DatabaseReference dbRef = dataBaseReference.child(Const.ContentsPATH).child(userID).child(mQuestion.getQuestionUid());
        //参照のリスナを設定して、同Titleのお気に入りデータがあれば、下記ボタンアクションを「お気に入り済みにする」
        dbRef.addChildEventListener(mEventListener);

        //お気に入りレコード作成ボタンアクション
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みかどうかのチェック
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference dbRef = dataBaseReference.child(Const.ContentsPATH).child(userID).child(mQuestion.getQuestionUid());
            //DatabaseReference dbRef = dataBaseReference.child(Const.ContentsPATH).child(userID);
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                // ログイン後ならデータのセット
                Map<String, String> data = new HashMap<String, String>();
                data.put("id", mQuestion.getQuestionUid());
                data.put("title",mQuestion.getTitle() );
                data.put("body",mQuestion.getBody() );
                dbRef.push().setValue(data);
            }
            }
        });
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

        try {
            if (databaseError == null) {
                finish();
            } else {
                //Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
                Log.d("ever", "ここまできたよ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}


