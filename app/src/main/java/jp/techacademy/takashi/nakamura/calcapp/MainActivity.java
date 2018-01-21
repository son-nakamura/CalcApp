package jp.techacademy.takashi.nakamura.calcapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MAX_DIGITS = 16;   // 最大表示桁数
    String answer = "";  // AnswerActivityへ送る文字列
    boolean error = false;  // エラーフラグ
    EditText editText1;
    EditText editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);

        Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(this);
        Button buttonMinus = (Button) findViewById(R.id.butonMinus);
        buttonMinus.setOnClickListener(this);
        Button buttonMultiply = (Button) findViewById(R.id.buttonMultiply);
        buttonMultiply.setOnClickListener(this);
        Button buttonDivide = (Button) findViewById(R.id.buttonDivide);
        buttonDivide.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String number1 = editText1.getText().toString();
        String number2 = editText2.getText().toString();

        // number1とnumber2をBigDecimalに変換。
        // number1とnumber2が数字と符号と小数点を含んでいたらNumberFormatExceptionをスローされる。
        BigDecimal bigDecimalNum1 = BigDecimal.ZERO;
        BigDecimal bigDecimalNum2 = BigDecimal.ZERO;
        try {
            bigDecimalNum1 = toBigDecimal(number1);
            bigDecimalNum2 = toBigDecimal(number2);
        } catch (NumberFormatException e) {
            answer = "エラー:数値以外入力";
            error = true;
        }

        if (!error) {
            // 演算ボタンにより処理を分岐
            switch (view.getId()) {
                case R.id.buttonPlus:
                    answer = bigDecimalNum1.add(bigDecimalNum2).toPlainString();
                    break;
                case R.id.butonMinus:
                    answer = bigDecimalNum1.subtract(bigDecimalNum2).toString();
                    break;
                case R.id.buttonMultiply:
                    answer = bigDecimalNum1.multiply(bigDecimalNum2).toPlainString();
                    break;
                case R.id.buttonDivide:
                    // ０で割った場合ArithmeticExceptionが発生。
                    try {
                        answer = bigDecimalNum1.divide(bigDecimalNum2,MAX_DIGITS - 1,
                                RoundingMode.DOWN).toPlainString();
                    } catch (ArithmeticException e) {
                        answer = "エラー:０で割った";
                        error = true;
                    }
                    break;
            }
        }

        // エラーでないとき
        if (!error && !answer.isEmpty()) {
            // 小数点以下の不要な０を削除
            answer = trimZero(answer);
            // OVERFLOWとUNDERFLOWを判定
            double dbAnswer = Double.valueOf(answer).doubleValue();
            if (dbAnswer >= Math.pow(10.0, MAX_DIGITS)) {
                answer = "エラー：計算結果が大きすぎます";
                error = true;
            } else if (dbAnswer < Math.pow(0.1, MAX_DIGITS - 1.0)) {
                answer = "エラー：計算結果が小さすぎます";
                error = true;
            }
       }

/*      テスト：計算結果をLogに出力
        if (error) {
            Log.d("CALC_TEST", answer);
        } else {
            Log.d("CALC_TEST", "答えは:" + answer);
        }
*/

        // AnswerActivityへanswerを送る。
        Intent intent = new Intent(this, AnswerActivity.class);
        intent.putExtra("ANSWER", answer);
        startActivity(intent);
    }


    // EditTextのインスタンスをBigDecimalのインスタンスに変換して戻すメソッド。
    // 引数に数字と符号と小数点以外の文字が含まれていたらNumberFormatExceptionをスローする。
    private BigDecimal toBigDecimal(String number) {
        BigDecimal bigDecimalNum;
        try {
            bigDecimalNum = new BigDecimal(number);
        } catch (NumberFormatException e) {
            throw e;
        }
        return bigDecimalNum;
    }


    // 文字列で渡された数値の小数点以下の不要な０を削除して戻すメソッド。
    private String trimZero(String number) {
        StringBuffer mainBuilder = new StringBuffer();
        StringBuffer subBuilder = new StringBuffer();
        // 小数点があるとindexOf()メソッドが-1を返すのでindexOfDecimalPointは0になる。
        int indexOfDecimalPoint = number.indexOf('.') + 1;
        int i = 0;

        // numberが小数でない場合はそのまま戻してreturnする。
        if (indexOfDecimalPoint == 0) {
            return number;
        }

        // 0.1のように0から始まる小数の場合、mainBuilderに"0."をappendしiを2に進める。
        if (number.charAt(0) == '0' && number.charAt(1) == '.') {
            mainBuilder.append("0.");
            i = 2;
        }

        // 整数部と小数点をmainBuilderにappendし、iを小数点の次に進める。
        else {
            mainBuilder.append(number.substring(0, indexOfDecimalPoint));
            i = indexOfDecimalPoint;
        }

        // 小数点以下の不要な０の探索と削除
        while (i <= number.length() - 1) {
            // 数字が0でなければmainBuilderにappendしてiを1進めてwhileループを次に進める。
            if (number.charAt(i) != '0') {
                mainBuilder.append(number.charAt(i++));
                continue;
            }
            // 0がみつかった場合、次に0以外の数字がみつかるまでsubBuilderに0をappendしていく。
            subBuilder.append('0');  // 前のif文でみつけた0
            for (int j = ++i; j <= number.length() - 1; j++) {  // 前のif文でiを1進めていないので、
                if (number.charAt(j) == '0') {        // iを1進めそこからループを始める。
                    subBuilder.append("0");           // 0が連続したらsubBuilderに0をappendめて、
                    i++;                              // iがjと同じ値に進むようにして、
                    continue;                        // forループを次に進める。
                // 0の連続が終わり0以外の数字がみつかった場合
                } else {
                    mainBuilder.append(subBuilder);   // subBuilderをmainBuilderにappendし、
                    subBuilder.delete(0, subBuilder.length());  // subBuilderの内容を消去し、
                    mainBuilder.append(number.charAt(j)); // jの位置の数字をmainBuilderにappendし、
                    i++;                // iがjと同じ値に進むようにしてforループを次に進む。
                }
            }   // 最後まで0が続いた場合はsubBuilderをmainBuilderにappendせずにforループが終わる。
        }

        // 小数点以下がすべて0であった場合、mainBuilderの末尾に小数点が残るので削除する。
        if (mainBuilder.charAt(mainBuilder.length() - 1) == '.') {
            mainBuilder.deleteCharAt(mainBuilder.length() - 1);
        }

        return mainBuilder.toString();
    }

}
