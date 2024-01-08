package com.example.iot;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlarmActivityTest {

    private AlarmActivity alarmActivity;

    @Before
    public void setUp() {
        alarmActivity = new AlarmActivity();
        alarmActivity.onCreate(null);
    }

    @Test
    public void testInitialState() {
        assertFalse(alarmActivity.isSensing);
        assertFalse(alarmActivity.isSimulating);
        assertEquals("None", alarmActivity.tvState.getText().toString());
        assertEquals("-", alarmActivity.tvReceivedTemperature.getText().toString());
        assertFalse(alarmActivity.btApplySimulation.isEnabled());
        assertFalse(alarmActivity.sliderSimulation.isEnabled());
    }

    @Test
    public void testSensorModeSelected() {
        alarmActivity.toggleButtonTempModeListener.onButtonChecked(null, R.id.btSensor, true);
        assertTrue(alarmActivity.isSensing);
        assertFalse(alarmActivity.isSimulating);
        assertEquals(STATE_SENSING, alarmActivity.tvState.getText().toString());
        assertEquals(String.valueOf(Utils.getInstance(alarmActivity.getApplicationContext()).temp_value_received),
                alarmActivity.tvReceivedTemperature.getText().toString());
        assertFalse(alarmActivity.btApplySimulation.isEnabled());
        assertFalse(alarmActivity.sliderSimulation.isEnabled());
    }

    @Test
    public void testSimulationModeSelected() {
        alarmActivity.toggleButtonTempModeListener.onButtonChecked(null, R.id.btSimulation, true);
        assertFalse(alarmActivity.isSensing);
        assertTrue(alarmActivity.isSimulating);
        assertEquals(STATE_SIMULATING, alarmActivity.tvState.getText().toString());
        assertEquals("-", alarmActivity.tvReceivedTemperature.getText().toString());
        assertTrue(alarmActivity.btApplySimulation.isEnabled());
        assertTrue(alarmActivity.sliderSimulation.isEnabled());
    }

    @Test
    public void testApplySimulationButtonClick() {
        alarmActivity.toggleButtonTempModeListener.onButtonChecked(null, R.id.btSimulation, true);
        float simulatedTemperature = 25.0f;
        alarmActivity.sliderSimulation.setValue(simulatedTemperature);
        alarmActivity.applySimulationButtonClickListener.onClick(null);
        assertEquals(String.valueOf(simulatedTemperature), alarmActivity.tvReceivedTemperature.getText().toString());
    }

    @Test
    public void testNewAlarmButtonClick() {
        assertFalse(alarmActivity.alertDialog != null && alarmActivity.alertDialog.isShowing());
        alarmActivity.newAlarmButtonClickListener.onClick(null);
        assertTrue(alarmActivity.alertDialog != null && alarmActivity.alertDialog.isShowing());
    }
}

