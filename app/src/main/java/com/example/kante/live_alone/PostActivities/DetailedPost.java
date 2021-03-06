package com.example.kante.live_alone.PostActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.kante.live_alone.Adapters.CommentAdapter;
import com.example.kante.live_alone.Classes.Comment;
import com.example.kante.live_alone.Classes.Like;
import com.example.kante.live_alone.Classes.User;
import com.example.kante.live_alone.MyMenuActivities.MyMenu;
import com.example.kante.live_alone.MyMenuActivities.MyMessages;
import com.example.kante.live_alone.R;
import com.example.kante.live_alone.MessageActivities.SendMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailedPost extends AppCompatActivity {
    private FirebaseStorage fs;
    private ImageView dImage;
    private TextView dTitle;
    private TextView dBody;
    private TextView dUid;
    private TextView dTime;
    private TextView note;
    private String dUrl;
    private StorageReference sr;
    private Button deleteButton;
    private String currentUserId;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private User user;
    private String post_id;
    private Button writeCommentButton;
    private Button findLocationButton;
    private EditText contextComment;
    private ImageView buttonLike;
    private ImageButton sendMessagebtn;

    private ListView commentListView;
    private CommentAdapter adapter;
    private List<Comment> comments;

//    private Post p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);

        //유저정보와 글쓴이가 일치하다면 포스트 삭제버튼 활성화
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        String user_id = firebaseAuth.getUid(); // 유저버튼 받아옴
        firebaseFirestore.collection("users").document(user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user = documentSnapshot.toObject(User.class);
                            if(user.getNickname().equals(dUid.getText().toString())){
                                deleteButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        dImage = findViewById(R.id.dp_image);
        dTitle = findViewById(R.id.dp_title);
        dBody = findViewById(R.id.dp_body);
        dUid = findViewById(R.id.dp_user_id);
        dTime = findViewById(R.id.dp_posted_time);
        deleteButton = findViewById(R.id.postDelete);
        writeCommentButton = findViewById(R.id.btn_comment_input);
        contextComment = findViewById(R.id.input_comment_context);
        buttonLike = findViewById(R.id.btn_like);
        findLocationButton = findViewById(R.id.find_location);
        note = findViewById(R.id.note);
        sendMessagebtn = findViewById(R.id.btn_send_message);
        Intent intent = getIntent();
        dTitle.setText(intent.getStringExtra("TITLE"));
        dBody.setText(intent.getStringExtra("BODY"));
        dUid.setText(intent.getStringExtra("UID"));
        dTime.setText(intent.getStringExtra("TIME"));

        //음식점 추천 카테고리 글에만 위치 검색기능 버튼 활성화
        if(intent.getStringExtra("CATEGORY").equals("FEatout")){
            findLocationButton.setVisibility(View.VISIBLE);
            findLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent searchIntent = new Intent();
                    searchIntent.setAction(Intent.ACTION_VIEW);
                    searchIntent.setData(Uri.parse("geo:0,0?q="+dTitle.getText()));
                    startActivity(searchIntent);
                }
            });
        }

        //중고품 거래 기능 글에만 note 텍스트뷰 보이게
        if(intent.getStringExtra("CATEGORY").equals("FTrans")){
            note.setVisibility(View.VISIBLE);
        }

        dUrl = getIntent().getStringExtra("URL");
        if(dUrl != null){
            dImage.setVisibility(View.VISIBLE);
        }
        post_id = getIntent().getStringExtra("POSTID");

        fs = FirebaseStorage.getInstance();
        sr = fs.getReferenceFromUrl("gs://hcslivealone.appspot.com");
        if(dUrl != null){
            StorageReference path = sr.child(dUrl);
            Glide.with(this).load(path).skipMemoryCache(true).into(dImage);
        }

        Log.d("qweqweqwe",intent.getStringExtra("posting_user_id"));
        Log.d("qweqweqwe",firebaseAuth.getUid());

        if(!firebaseAuth.getUid().equals(intent.getStringExtra("posting_user_id"))){
            Log.d("qweqweqwe",intent.getStringExtra("posting_user_id"));
            Log.d("qweqweqwe",firebaseAuth.getUid());
            sendMessagebtn.setVisibility(View.VISIBLE);
        }

        commentListView = (ListView)findViewById(R.id.list_comments);
        //댓글 보이기
        getComments();

        //처음에 시작할 때 자기가 좋아요한 글일 경우 좋아요 이미지가 활성화 되어있게 변경
        isLikePost();

        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getComments();
                swipeContainer.setRefreshing(false);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
    }


    public void menuClick(View v){
        PopupMenu popup = new PopupMenu(getApplicationContext(), v);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.go_mymenu:
                        Intent intent = new Intent(DetailedPost.this, MyMenu.class);
                        intent.putExtra("nickname",user.getNickname());
                        startActivity(intent);
                        break;
                    case R.id.messages:
                        Intent i = new Intent(DetailedPost.this, MyMessages.class);
                        startActivity(i);
                        break;
                }
                return false;
            }
        });
        popup.show();
    }


    public void deletePost(){
        firebaseFirestore.collection("posts").document(post_id).delete();
    }

    public void deletePostAffirm(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("포스트 삭제");
        builder.setMessage("정말 삭제하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();
                        Toast.makeText(getApplicationContext(),"포스트가 삭제되었습니다.",Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }





    /**********************************
     *             댓글 관련            *
     **********************************/

    public void getComments(){
//        if (!comments.isEmpty())
////            comments.clear();
        Log.d("qweqweqwe", "aaaaaaaa");
        firebaseFirestore.collection("comments").whereEqualTo("post_id",post_id).whereEqualTo("status","active").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()){
                            return;
                        } else{
                            comments = queryDocumentSnapshots.toObjects(Comment.class);
                            comments.sort(new CommentComparator());
                            adapter = new CommentAdapter(DetailedPost.this, comments);
                            commentListView.setAdapter(adapter);
                            setListViewHeightBasedOnChildren(commentListView);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("qweqweqwe", "아무내용이없습니다");
                    }
                });



    }

    //댓글 수만큼 listView 크기 설정
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        CommentAdapter listAdapter = (CommentAdapter) listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public void writeComment(){
        WriteBatch batch = firebaseFirestore.batch();
        DocumentReference comment = firebaseFirestore.collection("comments").document();
        Map<String, Object> docData = new HashMap<>();

        String context = contextComment.getText().toString();
        docData.put("context", context);
        docData.put("user_id", firebaseAuth.getUid());
        docData.put("nickname", user.nickname);
        docData.put("post_id", post_id);
        docData.put("status","active");
        docData.put("id", comment.getId());

        // 댓글 날짜 DB
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMddkkmmss");
        String format = s.format(new Date());

        docData.put("created_at",format);

        batch.set(comment,docData);
        batch.commit();
    }


    public void onClickCommentDelete(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("댓글 삭제");
        builder.setMessage("정말 삭제하시겠습니까?");

        int a = commentListView.getPositionForView(v);
        final String commentID = comments.get(a).id;
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseFirestore.collection("comments").document(commentID).update("status", "deactivated");
                        Toast.makeText(getApplicationContext(),"댓글이 삭제되었습니다.",Toast.LENGTH_LONG).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }


    //댓글 시간순으로 정렬
    public class CommentComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment o1, Comment o2) {
            return o1.getCreated_at().compareTo(o2.getCreated_at());
        }
    }




    /**********************************
     *             좋아요 관련            *
     **********************************/


    public void isLikePost(){
        firebaseFirestore.collection("likes").whereEqualTo("post_id",post_id).whereEqualTo("user_id",firebaseAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            Like l = queryDocumentSnapshots.toObjects(Like.class).get(0);
                            if(l.status.equals("active")){
                                buttonLike.setImageResource(R.drawable.like_clicked);
                            }
                        }else{
                            return;
                        }
                    }
                });
    }



    public void onClickwriteComment(View v){
        writeComment();
        Toast.makeText(this, "댓글이 등록되었습니다!", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(getIntent());
    }


    public void onClickLike(View v){

        firebaseFirestore.collection("likes").whereEqualTo("post_id",post_id).whereEqualTo("user_id",firebaseAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()){
                            /* 좋아요 객체가 없을때 */
                            WriteBatch batch = firebaseFirestore.batch();
                            DocumentReference like = firebaseFirestore.collection("likes").document();
                            Map<String, Object> docData = new HashMap<>();

                            docData.put("user_id", firebaseAuth.getUid());
                            docData.put("post_id", post_id);
                            docData.put("id", like.getId());

                            // 댓글 날짜 DB
                            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddkkmmss");
                            String format = s.format(new Date());

                            docData.put("created_at",format);
                            docData.put("status", "active");
                            buttonLike.setImageResource(R.drawable.like_clicked);

                            batch.set(like,docData);
                            batch.commit();

                            Toast.makeText(getApplicationContext(),"좋아요",Toast.LENGTH_LONG).show();
                        }else {
                            /* 좋아요 객체가 있을 때*/
                            Like l = queryDocumentSnapshots.toObjects(Like.class).get(0);
                            if (l.status.equals("active")) {
                                Log.d("qweasdzxc", "zxc");
                                firebaseFirestore.collection("likes").document(l.id).update("status", "deactivated");
                                buttonLike.setImageResource(R.drawable.like);
                                Toast.makeText(getApplicationContext(),"좋아요 취소",Toast.LENGTH_LONG).show();
                            } else {
                                Log.d("qweasdzxc", "qqq");
                                firebaseFirestore.collection("likes").document(l.id).update("status", "active");
                                buttonLike.setImageResource(R.drawable.like_clicked);
                                Toast.makeText(getApplicationContext(),"좋아요",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                WriteBatch batch = firebaseFirestore.batch();
                DocumentReference like = firebaseFirestore.collection("likes").document();
                Map<String, Object> docData = new HashMap<>();

                docData.put("user_id", firebaseAuth.getUid());
                docData.put("post_id", post_id);
                docData.put("id", like.getId());

                // 댓글 날짜 DB
                SimpleDateFormat s = new SimpleDateFormat("yyyyMMddkkmmss");
                String format = s.format(new Date());

                docData.put("created_at",format);
                docData.put("status", "active");
                buttonLike.setImageResource(R.drawable.like_clicked);

                batch.set(like,docData);
                batch.commit();
            }
        });
    }

    /**********************************
     *             쪽지 관련            *
     **********************************/
    public void onClicksendMessage(View v){
        Intent intent = new Intent(this, SendMessage.class);
        intent.putExtra("receiver_id",getIntent().getStringExtra("posting_user_id"));
        intent.putExtra("receiver_nickname",dUid.getText().toString());
        startActivity(intent);
    }

}
