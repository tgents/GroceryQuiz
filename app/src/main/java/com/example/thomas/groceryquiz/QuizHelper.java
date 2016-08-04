package com.example.thomas.groceryquiz;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by thomas on 8/4/16.
 */
public class QuizHelper {

    private static final String TAG = "QuizHelper";
    private List<Question> questions;
    private int position = 0;
    private int[] questionOrder;
    private String[] userAnswers;

    public QuizHelper(String json) throws JSONException {
        JSONObject parse = new JSONObject(json);
        Iterator<String> parsekeys = parse.keys();
        questions = new ArrayList<Question>();
        while (parsekeys.hasNext()) {
            String word = parsekeys.next();
            JSONArray images = parse.getJSONArray(word);
            questions.add(new Question(word, images));
        }
        questionOrder = new int[questions.size()];
        userAnswers = new String[questions.size()];
        for (int i = 0; i < questionOrder.length; i++) {
            questionOrder[i] = i;
        }
        shuffle(questionOrder);
    }

    public static void shuffle(int[] array) {
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    public static void shuffle(String[] array) {
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    public Question nextQuestion() {
        if(position + 1 > questions.size()){
            return null;
        }
        return questions.get(questionOrder[position]);
    }

    public void addAnswer(String answer){
        userAnswers[position] = answer;
        position++;
    }

    public int numCorrect() {
        int numCorrect = 0;
        for(int i = 0; i < userAnswers.length; i++){
            String userAnswer = userAnswers[i];
            String answer = questions.get(questionOrder[i]).getAnswer();
            if(answer.equals(userAnswer)){
                numCorrect++;
            }
        }
        return numCorrect;
    }

    public int numQuestions() {
        return questions.size();
    }

    public void restartQuiz(){
        shuffle(questionOrder);
        position = 0;
    }

    public class Question{
        public String prompt;
        public List<String> answers;
        public Question(String prompt, JSONArray answers) throws JSONException {
            this.prompt = prompt;
            this.answers = new ArrayList<String>();
            for (int i = 0; i < answers.length(); i++) {
                String item = answers.getString(i);
                this.answers.add(item);
            }
        }

        public String getAnswer(){
            return answers.get(0);
        }
    }
}
