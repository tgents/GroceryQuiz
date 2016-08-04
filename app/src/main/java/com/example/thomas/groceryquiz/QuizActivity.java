package com.example.thomas.groceryquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    private CountDownTimer countdown;
    private ProgressBar timer;
    private GridLayout grid;
    private NetworkImageView[] displays;
    private TextView prompt;
    private Button submit;

    private QuizHelper quiz;
    private ImageLoader imageloader;
    private String userAnswer;
    private String[] answerPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        try {
            quiz = new QuizHelper(loadJSONFromAsset("questions.json"));
        } catch (JSONException e) {
            new AlertDialog.Builder(QuizActivity.this)
                    .setMessage("Could not load questions...")
                    .setCancelable(false)
                    .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            returnToHome();
                        }
                    }).show();
        }
        imageloader = NetworkImageHelper.getInstance(this).getImageLoader();

        prompt = (TextView) findViewById(R.id.prompt);

        grid = (GridLayout) findViewById(R.id.grid);
        displays = new NetworkImageView[grid.getChildCount()];
        for (int i = 0; i < grid.getChildCount(); i++) {
            displays[i] = (NetworkImageView) grid.getChildAt(i);
            final int j = i;
            displays[j].setOnClickListener(new View.OnClickListener() {
                public String answer;

                @Override
                public void onClick(View view) {
                    userAnswer = answerPos[j];
                    for (int i = 0; i < displays.length; i++) {
                        displays[i].setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light));
                    }
                    displays[j].setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                }
            });
        }
        submit = (Button) findViewById(R.id.submitBtn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAnswer == null) {
                    Toast.makeText(QuizActivity.this, "Please select an answer.", Toast.LENGTH_SHORT);
                } else {
                    quiz.addAnswer(userAnswer);
                    userAnswer = null;
                    QuizHelper.Question nextQuestion = quiz.nextQuestion();
                    if (nextQuestion == null) {
                        new AlertDialog.Builder(QuizActivity.this)
                                .setTitle("Done!")
                                .setMessage("You got " + quiz.numCorrect() + " correct out of " + quiz.numQuestions() + " in " + (120 - timer.getProgress()) + " seconds")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        returnToHome();
                                    }
                                })
                                .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        restartQuiz();
                                    }
                                }).show();
                    } else {
                        populateQuestion(nextQuestion);
                    }
                }
            }
        });

        populateQuestion(quiz.nextQuestion());
        startTimer();
    }

    private void populateQuestion(QuizHelper.Question question) {
        for (int i = 0; i < displays.length; i++) {
            displays[i].setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light));
        }
        prompt.setText(question.prompt);
        answerPos = new String[displays.length];
        question.answers.toArray(answerPos);
        QuizHelper.shuffle(answerPos);
        for (int i = 0; i < displays.length; i++) {
            displays[i].setImageUrl(answerPos[i], imageloader);
        }
    }

    private void startTimer() {
        if (timer == null) {
            timer = (ProgressBar) findViewById(R.id.timer);
        }
        long starttime = 120000;
        if(countdown != null){
            countdown.cancel();
        }
        countdown = new CountDownTimer(starttime, 500) {
            public void onTick(long millisUntilFinished) {
                if(android.os.Build.VERSION.SDK_INT >= 24){
                    timer.setProgress((int) (millisUntilFinished / 1000), true);
                }
                else
                    timer.setProgress((int) (millisUntilFinished / 1000));

            }

            public void onFinish() {
                if (QuizActivity.this != null) {
                    new AlertDialog.Builder(QuizActivity.this)
                            .setTitle("Done!")
                            .setMessage("You got " + quiz.numCorrect() + " correct")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    returnToHome();
                                }
                            })
                            .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    restartQuiz();
                                }
                            }).show();
                }
            }
        }.start();
    }

    private void restartQuiz() {
        quiz.restartQuiz();
        populateQuestion(quiz.nextQuestion());
        startTimer();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quiz, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.quitBtn:
                returnToHome();
                return true;
            case R.id.restartBtn:
                restartQuiz();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void returnToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        countdown.cancel();
        super.finish();
    }

    public String loadJSONFromAsset(String assetName) {
        String json = null;
        try {
            InputStream is = getAssets().open(assetName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
