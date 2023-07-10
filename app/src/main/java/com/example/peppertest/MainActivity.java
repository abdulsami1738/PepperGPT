package com.example.peppertest;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ApproachHumanBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.human.Age;
import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.Emotion;
import com.aldebaran.qi.sdk.object.human.EngagementIntentionState;
import com.aldebaran.qi.sdk.object.human.FacialExpressions;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.ApproachHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.image.TimestampedImage;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Region;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.*;
import android.widget.Toast;


import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/* loaded from: classes3.dex */
public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    ImageButton voice_btn;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    MediaPlayer player;

    GoTo goTo;

    private LocalizeAndMap localizeAndMap;
    private Future<Void> localizationAndMapping;
    private ExplorationMap explorationMap;
    private Localize localize;




    int size = 0;
    boolean check = false;
    String heard = "";

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.aldebaran.qi.sdk.design.activity.RobotActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_main);

        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        voice_btn = findViewById(R.id.voice_btn);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim();
            addToChat(question,Message.SENT_BY_ME);
            size = messageList.size();
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });


        voice_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
    }
    void speak(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e){
//            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if(resultCode==RESULT_OK && null!=data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addToChat(result.get(0), Message.SENT_BY_ME);
                    callAPI(result.get(0));
                    welcomeTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override // com.aldebaran.qi.sdk.RobotLifecycleCallbacks
    public void onRobotFocusGained(QiContext qiContext) {
        move(qiContext);
        if(!heard.equals("")){
            say(qiContext);
        }else{
            listen(qiContext);
        }
        while (true) {
            say(qiContext);
        }

    }
    void say(QiContext qiContext){
        boolean done = false;
        String toSay = "";
        if(messageList.isEmpty()) {
            done = false;
        }
        else if (messageList.size() > size && messageList.get(messageList.size()-1).getSentBy().equals("bot") && !check){
            toSay = messageList.get(messageList.size()-1).getMessage();
            done = true;

        } else if (messageList.get(messageList.size()-1).getSentBy().equals("me")) {
            check = false;
        }

        Phrase phrase = new Phrase(toSay);
        Say say = SayBuilder.with(qiContext).withPhrase(phrase).build();
        if (done) {
            say.run();
            check = true;
            speak();
        }
    }

    void listen(QiContext qiContext){
        PhraseSet phraseSet = PhraseSetBuilder.with(qiContext).withTexts("Hello","Hey","Pepper","hail","hitler").build();
        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSet(phraseSet)
                .build();
        ListenResult listenResult = listen.run();
        heard = listenResult.toString();
        callAPI(heard);
    }
    void move(QiContext qiContext){
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();
        Transform transform = TransformBuilder.create().fromXTranslation(2);
        Transform transform2 = TransformBuilder.create().from2DTransform(2,2,3);
        Mapping mapping = qiContext.getMapping();
        FreeFrame targetFrame = mapping.makeFreeFrame();
        targetFrame.update(robotFrame,transform2,0L);

        goTo = GoToBuilder.with(qiContext).withFrame(targetFrame.frame()).build();

        Future<Void> goToFuture = goTo.async().run();

    }

    void localize(QiContext qiContext){
        LocalizeAndMap localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();
        localizeAndMap.async().run();

        Future<Void> localizationAndMapping;

        ExplorationMap explorationMap = localizeAndMap.dumpMap();

    }
    void startMapping(QiContext qiContext){
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

        // Add an on status changed listener on the LocalizeAndMap action to know when the robot has mapped his environment.
        localizeAndMap.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    // Dump the ExplorationMap.
                    explorationMap = localizeAndMap.dumpMap();

                    Log.i(TAG, "Robot has mapped his environment.");

                    // Cancel the LocalizeAndMap action.
                    localizationAndMapping.requestCancellation();
                    break;
            }
        });

        Log.i(TAG, "Mapping...");

        // Execute the LocalizeAndMap action asynchronously.
        localizationAndMapping = localizeAndMap.async().run();

        // Add a lambda to the action execution.
        localizationAndMapping.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "LocalizeAndMap action finished with error.", future.getError());
            } else if (future.isCancelled()) {
                // The LocalizeAndMap action has been cancelled.
            }
        });
    }
    void startLocalizing(QiContext qiContext){
        localize = LocalizeBuilder.with(qiContext)
                .withMap(explorationMap)
                .build();

        // Add an on status changed listener on the Localize action to know when the robot is localized in the map.
        localize.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    Log.i(TAG, "Robot is localized.");
                    break;
            }
        });

        Log.i(TAG, "Localizing...");

        // Execute the Localize action asynchronously.
        Future<Void> localization = localize.async().run();

        // Add a lambda to the action execution.
        localization.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Localize action finished with error.", future.getError());
            }
        });
    }
//    void approach(QiContext qiContext){
//        Human human = new Human() {
//            @Override
//            public Async async() {
//                return null;
//            }
//
//            @Override
//            public Frame getHeadFrame() {
//                return null;
//            }
//
//            @Override
//            public Emotion getEmotion() {
//                return null;
//            }
//
//            @Override
//            public EngagementIntentionState getEngagementIntention() {
//                return null;
//            }
//
//            @Override
//            public void setOnEngagementIntentionChangedListener(OnEngagementIntentionChangedListener onEngagementIntentionChangedListener) {
//
//            }
//
//            @Override
//            public void addOnEngagementIntentionChangedListener(OnEngagementIntentionChangedListener onEngagementIntentionChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnEngagementIntentionChangedListener(OnEngagementIntentionChangedListener onEngagementIntentionChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnEngagementIntentionChangedListeners() {
//
//            }
//
//            @Override
//            public FacialExpressions getFacialExpressions() {
//                return null;
//            }
//
//            @Override
//            public void setOnFacialExpressionsChangedListener(OnFacialExpressionsChangedListener onFacialExpressionsChangedListener) {
//
//            }
//
//            @Override
//            public void addOnFacialExpressionsChangedListener(OnFacialExpressionsChangedListener onFacialExpressionsChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnFacialExpressionsChangedListener(OnFacialExpressionsChangedListener onFacialExpressionsChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnFacialExpressionsChangedListeners() {
//
//            }
//
//            @Override
//            public AttentionState getAttention() {
//                return null;
//            }
//
//            @Override
//            public void setOnAttentionChangedListener(OnAttentionChangedListener onAttentionChangedListener) {
//
//            }
//
//            @Override
//            public void addOnAttentionChangedListener(OnAttentionChangedListener onAttentionChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnAttentionChangedListener(OnAttentionChangedListener onAttentionChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnAttentionChangedListeners() {
//
//            }
//
//            @Override
//            public Age getEstimatedAge() {
//                return null;
//            }
//
//            @Override
//            public void setOnEstimatedAgeChangedListener(OnEstimatedAgeChangedListener onEstimatedAgeChangedListener) {
//
//            }
//
//            @Override
//            public void addOnEstimatedAgeChangedListener(OnEstimatedAgeChangedListener onEstimatedAgeChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnEstimatedAgeChangedListener(OnEstimatedAgeChangedListener onEstimatedAgeChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnEstimatedAgeChangedListeners() {
//
//            }
//
//            @Override
//            public Gender getEstimatedGender() {
//                return null;
//            }
//
//            @Override
//            public void setOnEstimatedGenderChangedListener(OnEstimatedGenderChangedListener onEstimatedGenderChangedListener) {
//
//            }
//
//            @Override
//            public void addOnEstimatedGenderChangedListener(OnEstimatedGenderChangedListener onEstimatedGenderChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnEstimatedGenderChangedListener(OnEstimatedGenderChangedListener onEstimatedGenderChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnEstimatedGenderChangedListeners() {
//
//            }
//
//            @Override
//            public TimestampedImage getFacePicture() {
//                return null;
//            }
//
//            @Override
//            public void setOnFacePictureChangedListener(OnFacePictureChangedListener onFacePictureChangedListener) {
//
//            }
//
//            @Override
//            public void addOnFacePictureChangedListener(OnFacePictureChangedListener onFacePictureChangedListener) {
//
//            }
//
//            @Override
//            public void removeOnFacePictureChangedListener(OnFacePictureChangedListener onFacePictureChangedListener) {
//
//            }
//
//            @Override
//            public void removeAllOnFacePictureChangedListeners() {
//
//            }
//        };
//
//        ApproachHuman approachHuman = ApproachHumanBuilder.with(qiContext)
//                .withHuman(human)
//                .build();
//
//// Run the action asynchronously.
//        approachHuman.async().run();
//
//    }
    @Override // com.aldebaran.qi.sdk.RobotLifecycleCallbacks
    public void onRobotFocusLost() {
        if(goTo!=null){
            goTo.removeAllOnStartedListeners();
        }
        if (localizeAndMap != null) {
            localizeAndMap.removeAllOnStatusChangedListeners();
        }
        if(localize!=null){
            localize.removeAllOnStartedListeners();
        }
    }

    @Override // com.aldebaran.qi.sdk.RobotLifecycleCallbacks
    public void onRobotFocusRefused(String reason) {
    }

    @Override // com.aldebaran.qi.sdk.design.activity.RobotActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    public String addResponse(String response){
//        messageList.remove(messageList.size()-1);
        int responseLength = response.length();
        addToChat(response,Message.SENT_BY_BOT);
        return response;
    }
    private static final String API_KEY = "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";
    void callAPI(String question){
        //okhttp
//        messageList.add(new Message("Thinking... ",Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messageArr = new JSONArray();
            JSONObject obj1 = new JSONObject();
            JSONObject obj2 = new JSONObject();

            obj1.put("role", "system");
            obj1.put("content","You are a humanoid robot created by SoftBank Robotics. Your name is Pepper. Pepper is a robot designed for people." +
                    " Built to connect with them, assist them, and share knowledge with them – while helping your business in the process." +
                    " Friendly and engaging, Pepper creates unique experiences and forms real relationships. You like to give short, but precise and easy to understand answers that can be read aloud in under 15 seconds");
            obj2.put("role", "user");
            obj2.put("content",question);

            messageArr.put(obj1);
            messageArr.put(obj2);

            jsonBody.put("messages", messageArr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer "+API_KEY)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject  jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message").getString("content");
                       addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    addResponse("Failed to load response due to "+response.body().string());
                }
            }
        });
    }
}