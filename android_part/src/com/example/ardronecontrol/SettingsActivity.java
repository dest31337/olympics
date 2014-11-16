package com.example.ardronecontrol;

/**
 * Created with IntelliJ IDEA.
 * User: demo
 * Date: 25.05.13
 * Time: 0:14
 * To change this template use File | Settings | File Templates.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * Настройки, которые хранят адрес сервера, множитель для масштабирования скорости дрона и порог чувствительности к изменению направления
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateSummary(s);
    }

    protected void onResume() {
        updateSummary();
        super.onResume();
    }

    /**
     * Обновить подписи к настройкам, чтобы можно было сразу же наблюдать текущие настройки, без непосредственного их изменения
     */
    private void updateSummary() {
      for (String key : getPreferenceScreen().getSharedPreferences().getAll().keySet()) {
          updateSummary(key);
      }
    }

    /**
     * Обновление подписи к конкретной настройке
     * @param key Ключ настройки
     */
    private void updateSummary(String key) {
        EditTextPreference etp = (EditTextPreference) findPreference(key);
        etp.setSummary(etp.getText());
    }
}
