package com.stupid_genius.mathtutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class MainActivity extends AppCompatActivity {
	TextView firstNumber;
	TextView secondNumber;
	TextView operator;
	TextView answer;
	TextView result;
	TextView resultTop;
	TextView accuracy;
	TextView problemCount;

	Handler timerHandler = new Handler();
	Runnable resultTimer = new Runnable() {
		@Override
		public void run() {
			resultTop.setText("", TextView.BufferType.NORMAL);
		}
	};

	private MathTutor tutor;
	private SimpleProblem problem;
	private OperationEnum operation = OperationEnum.RANDOM;
	private int difficulty = 10;
	private boolean allowNegatives = false;

	public MainActivity() {
		tutor = new MathTutor();
	}

	private Template getTemplate(Context applicationContext, Configuration conf) throws IOException {
		InputStream ins = applicationContext.getResources().openRawResource(R.raw.worksheet);
		java.util.Scanner s = new java.util.Scanner(ins).useDelimiter("\\A");
		String template = s.hasNext() ? s.next() : "";
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("worksheet", template);
		conf.setTemplateLoader(stringLoader);
		return conf.getTemplate("worksheet");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setSupportActionBar(this.<Toolbar>findViewById(R.id.toolbar));

		firstNumber = findViewById(R.id.firstNumber);
		secondNumber = findViewById(R.id.secondNumber);
		operator = findViewById(R.id.operator);
		answer = findViewById(R.id.answer);
		result = findViewById(R.id.result);
		resultTop = findViewById(R.id.result_top);
		accuracy = findViewById(R.id.accuracy);
		problemCount = findViewById(R.id.totalProblems);

		//int id = this.getResources().getIdentifier("worksheet.html", "raw", this.getPackageName());
		Map<String, Object> modelRoot = Maps.newHashMap();
		Configuration config = new Configuration();
		config.setDefaultEncoding("UTF-8");
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		config.setLogTemplateExceptions(true);
		Template worksheetTemplate = null;
		try {
			//worksheetTemplate = config.getTemplate("worksheet.html");
			worksheetTemplate = getTemplate(getApplicationContext(), config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Writer out = new OutputStreamWriter(System.out);
		try {
			worksheetTemplate.process(modelRoot, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		startSession();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		MenuItem numberClass = menu.findItem(R.id.classSelected);
		numberClass.setTitle("Integers");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.allowNegatives:
				item.setChecked(!allowNegatives);
				allowNegatives = item.isChecked();
				break;
			case R.id.difficulty:
				final EditText editView = new EditText(MainActivity.this);
				editView.setInputType(InputType.TYPE_CLASS_NUMBER);
				AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("Set difficulty (10 - 99)")
					.setView(editView)
					.setPositiveButton("Set", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String value = String.valueOf(editView.getText());
							value = value.substring(0, value.length()>2?2:value.length());
							if (value.isEmpty()) {
								difficulty = 10;
							} else {
								difficulty = Integer.valueOf(value);
								if (difficulty < 10) {
									difficulty = 10;
								}
								if (difficulty > 99) {
									difficulty = 99;
								}
							}
							startSession();
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
				dialog.show();
				return true;
			case R.id.addition:
				operation = OperationEnum.ADDITION;
				break;
			case R.id.subtraction:
				operation = OperationEnum.SUBTRACTION;
				break;
			case R.id.multiplication:
				operation = OperationEnum.MULTIPLICATION;
				break;
			case R.id.division:
				operation = OperationEnum.DIVISION;
				break;
			case R.id.random:
				operation = OperationEnum.RANDOM;
				break;
			case R.id.integers:
				return true;
			case R.id.fractions:
				Intent intent = new Intent(MainActivity.this, FractionActivity.class);
				startActivity(intent);
				break;
			case R.id.worksheet:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		startSession();
		return true;
	}

	private void setFirstNumber(CharSequence s) {
		firstNumber.setText(s, TextView.BufferType.NORMAL);
	}

	private void setSecondNumber(CharSequence s) {
		secondNumber.setText(s, TextView.BufferType.NORMAL);
	}

	private void setOperator(CharSequence s) {
		operator.setText(s, TextView.BufferType.NORMAL);
	}

	private Integer getAnswer() {
		Integer userAnswer = 0;
		String answerText = answer.getText().toString();
		if (answerText.length() != 0) {
			userAnswer = Integer.valueOf(answerText);
		}
		return userAnswer;
	}

	private void setAnswer(CharSequence s) {
		answer.setText(s, TextView.BufferType.NORMAL);
	}

	private void setResult(CharSequence s) {
		result.setText(s, TextView.BufferType.NORMAL);
	}

	private void setResultTop(CharSequence s) {
		resultTop.setText(s, TextView.BufferType.NORMAL);
		timerHandler.postDelayed(resultTimer, 1000);
	}

	private void updateStats() {
		Double total = (double) tutor.getTotalProblems();
		Double percent = 100.0;
		if (total > 0) {
			percent = ((double) tutor.getCorrectProblems() / total)*100;
		}
		accuracy.setText(String.format("%d%%", percent.intValue()));
		problemCount.setText(String.format("out of %d", total.intValue()));
	}

	public void numberClick(View button) {
		TextView clickedButton = (TextView) button;
		String buttonText = String.valueOf(clickedButton.getText());
		String answerBuffer = String.valueOf(answer.getText());
		if (buttonText.equals("-") && (answerBuffer.contains(buttonText) || !answerBuffer.isEmpty())) {
			return;
		}
		answer.append(clickedButton.getText());
	}

	public void clearClick(View button) {
		setAnswer("");
	}

	public void submitClick(View view) {
		Integer userAnswer = getAnswer();
		boolean isCorrect = problem.checkAnswer(userAnswer);
		tutor.recordAnswer(isCorrect);
		updateStats();
		if (isCorrect) {
			setResult("CORRECT!");
			setResultTop("CORRECT!");
		} else {
			setResult(String.format("incorrect: %s %d", problem.toString(), userAnswer));
			setResultTop("incorrect");
		}
		if (tutor.hasNext()) {
			startProblem();
		} else {
			// something else
		}
	}

	private void startSession() {
		setResult("");
		setResultTop("");
		tutor.startSession(NumberEnum.INTEGER, operation, difficulty, allowNegatives);
		updateStats();
		startProblem();
	}

	private void startProblem() {
		problem = tutor.next();
		setFirstNumber(problem.getFirstNumber().toString());
		setSecondNumber(problem.getSecondNumber().toString());
		setOperator(problem.getOperator());
		setAnswer("");
	}
}
