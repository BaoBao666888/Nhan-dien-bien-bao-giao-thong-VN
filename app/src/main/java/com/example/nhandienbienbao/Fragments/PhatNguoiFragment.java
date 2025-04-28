package com.example.nhandienbienbao.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Adapter.ViolationAdapter;
import com.example.nhandienbienbao.Models.Violation;
import com.example.nhandienbienbao.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class PhatNguoiFragment extends Fragment {

    private WebView webCaptcha;
    private RecyclerView recyclerKetQua;
    private ArrayList<Violation> violationsList;
    private TextView txtThongKeKetQua;
    private ViolationAdapter adapter;
    private EditText edtBienSo;
    private Spinner spinnerLoaiXe;
    private ImageView btnLoadCaptcha;
    private Button btnTraCuu;
    private String turnstileToken = "";
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phatnguoi, container, false);

        edtBienSo = view.findViewById(R.id.edtBienSo);
        spinnerLoaiXe = view.findViewById(R.id.spinnerLoaiXe);
        btnLoadCaptcha = view.findViewById(R.id.btnLoadCaptcha);
        btnTraCuu = view.findViewById(R.id.btnTraCuu);
        webCaptcha = view.findViewById(R.id.webCaptcha);
        recyclerKetQua = view.findViewById(R.id.recyclerKetQua);
        violationsList = new ArrayList<>();
        adapter = new ViolationAdapter(violationsList);
        recyclerKetQua.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerKetQua.setAdapter(adapter);
        txtThongKeKetQua = view.findViewById(R.id.txtThongKeKetQua);

        setupSpinner();
        setupWebViewForCaptcha();
        setupLoadCaptchaButton();
        setupTraCuuButton();

        return view;
    }

    private void setupSpinner() {
        String[] loaiXeArray = {getString(R.string.o_to), getString(R.string.xe_may), getString(R.string.xe_may_dien)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, loaiXeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoaiXe.setAdapter(adapter);
    }

    private void setupWebViewForCaptcha() {
        WebSettings settings = webCaptcha.getSettings();
        settings.setJavaScriptEnabled(true);
        webCaptcha.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                webCaptcha.evaluateJavascript(
                        "(function() { var elems = document.querySelectorAll('iframe, object, embed, video'); elems.forEach(e => e.remove()); })();",
                        null
                );
                checkTurnstileTokenLoop();
            }
        });
        webCaptcha.loadUrl("https://phatnguoi.com");
    }

    private void setupLoadCaptchaButton() {
        btnLoadCaptcha.setOnClickListener(v -> {
            turnstileToken = "";
            btnLoadCaptcha.setImageResource(R.drawable.ic_loading);
            startRotateAnimation();
            webCaptcha.reload();
            checkTurnstileTokenLoop();
        });
    }

    private void startRotateAnimation() {
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        btnLoadCaptcha.startAnimation(rotate);
    }

    private void stopRotateAnimation() {
        btnLoadCaptcha.clearAnimation();
    }

    private void checkTurnstileTokenLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                webCaptcha.evaluateJavascript(
                        "(function() { var el = document.querySelector('input[name=\"cf-turnstile-response\"]'); return el ? el.value : ''; })();",
                        value -> {
                            value = value.replace("\"", "").trim();
                            if (!value.isEmpty() && !value.equals("null")) {
                                turnstileToken = value;
                                stopRotateAnimation();
                                btnLoadCaptcha.setImageResource(R.drawable.ic_success);
                                Log.d("TOKEN", "Turnstile Token OK: " + turnstileToken);
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        }
                );
            }
        }, 1000);
    }

    private void setupTraCuuButton() {
        btnTraCuu.setOnClickListener(v -> {
            if (turnstileToken.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.dang_tai_captcha_vui_long_cho), Toast.LENGTH_SHORT).show();
                return;
            }
            String bienSo = edtBienSo.getText().toString().trim();
            if (bienSo.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.nhap_bien_so_thong_bao), Toast.LENGTH_SHORT).show();
                return;
            }
            int loaixe = spinnerLoaiXe.getSelectedItemPosition() + 1;
            traCuuPhatNguoi(bienSo, turnstileToken, loaixe);
        });
    }

    private void traCuuPhatNguoi(String bienSo, String captchaToken, int loaiXe) {
        new Thread(() -> {
            try {
                URL url = new URL("https://phatnguoi.com/action.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "*/*");

                String boundary = UUID.randomUUID().toString();
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                writeFormField(dos, boundary, "type", "1");
                writeFormField(dos, boundary, "retry", "");
                writeFormField(dos, boundary, "loaixe", String.valueOf(loaiXe));
                writeFormField(dos, boundary, "bsx", bienSo);
                writeFormField(dos, boundary, "bsxdangkiem", "");
                writeFormField(dos, boundary, "bien", "T");
                writeFormField(dos, boundary, "tem", "");
                writeFormField(dos, boundary, "cf-turnstile-response", captchaToken);
                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    int ch;
                    while ((ch = is.read()) != -1) {
                        sb.append((char) ch);
                    }
                    is.close();
                    parseResult(sb.toString());
                } else {
                    requireActivity().runOnUiThread(() -> txtThongKeKetQua.setText("Lỗi server: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> txtThongKeKetQua.setText("Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }

    private void writeFormField(DataOutputStream dos, String boundary, String name, String value) throws Exception {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        dos.writeBytes(value + "\r\n");
    }

    private void parseResult(String json) {
        requireActivity().runOnUiThread(() -> {
            try {
                JSONObject data = new JSONObject(json);
                if (data.has("message") && data.getString("message").contains("Không có kết quả")) {
                    violationsList.clear();
                    adapter.notifyDataSetChanged();
                    txtThongKeKetQua.setText(data.getString("message"));
                    resetCaptcha();
                    return;
                }

                int totalViolations = data.optInt("totalViolations", 0);
                int unhandledCount = data.optInt("unhandledCount", 0);
                int handledCount = totalViolations - unhandledCount;

                String thongke = "Tổng lỗi: " + totalViolations +
                        "\nChưa xử phạt: " + unhandledCount +
                        "\nĐã xử phạt: " + handledCount;
                txtThongKeKetQua.setText(thongke);

                violationsList.clear();
                JSONArray violations = data.getJSONArray("violations");
                for (int i = 0; i < violations.length(); i++) {
                    JSONObject obj = violations.getJSONObject(i);
                    Violation v = new Violation();
                    v.bienKiemSoat = obj.optString("bien_kiem_sat", "");
                    v.thoiGian = obj.optString("thoi_gian_vi_pham", "");
                    v.diaDiem = obj.optString("dia_diem_vi_pham", "");
                    v.hanhVi = obj.optString("hanh_vi_vi_pham", "");
                    v.trangThai = obj.optString("trang_thai", "");
                    v.mucPhat = obj.optString("muc_phat", "");
                    violationsList.add(v);
                }
                adapter.notifyDataSetChanged();
                resetCaptcha();
            } catch (Exception e) {
                e.printStackTrace();
                txtThongKeKetQua.setText("Error: Lỗi xử lý dữ liệu!");
            }
        });
    }

    private void resetCaptcha() {
        turnstileToken = "";
        btnLoadCaptcha.setImageResource(R.drawable.ic_loading);
        startRotateAnimation();
        webCaptcha.reload();
        checkTurnstileTokenLoop();
    }
}