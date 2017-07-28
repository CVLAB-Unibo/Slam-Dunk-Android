package it.unibo.slam.gui.preferences;

import it.unibo.slam.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class EditTextFloatPreference extends DialogPreference
{
    /**
     * The edit text shown in the dialog.
     */
    private EditText mEditText;
    
    private float mValue;
    
    public EditTextFloatPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        
        setDialogLayoutResource(R.layout.edit_text_float_preference);
        
        mEditText = new EditText(context, attrs);
        
        // Give it an ID so it can be saved/restored
        mEditText.setId(R.id.edit);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mEditText.setEnabled(true);
    }
    
    public EditTextFloatPreference(Context context)
    {
        this(context, null);
    }
    
    /**
     * Saves the float value to the {@link SharedPreferences}.
     * 
     * @param value The float value to save.
     */
    public void setValue(float value)
    {
        final boolean wasBlocking = shouldDisableDependents();
        
        mValue = value;
        
        persistFloat(value);
        
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking)
            notifyDependencyChange(isBlocking);
    }
    
    /**
     * Gets the float value from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public float getValue()
    {
        return mValue;
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);
        
        EditText editText = mEditText;
        editText.setText(Float.toString(getValue()));
        
        ViewParent oldParent = editText.getParent();
        if (oldParent != view)
        {
            if (oldParent != null)
                ((ViewGroup) oldParent).removeView(editText);
            onAddEditTextToDialogView(view, editText);
        }
    }

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     * 
     * @param dialogView The dialog view.
     */
    protected void onAddEditTextToDialogView(View dialogView, EditText editText)
    {
        ViewGroup container = (ViewGroup) dialogView.
               	findViewById(R.id.edittext_container);
        if (container != null)
        {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult)
        {
            float value = Float.parseFloat(mEditText.getText().toString());
            if (callChangeListener(value))
            {
                setValue(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return a.getFloat(index, -1.0F);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        setValue(restoreValue ? getPersistedFloat(mValue) : Float.parseFloat(String.valueOf(defaultValue)));
    }

    @Override
    public boolean shouldDisableDependents()
    {
        return TextUtils.isEmpty(mEditText.getText()) || super.shouldDisableDependents();
    }

    /**
     * Returns the {@link EditText} widget that will be shown in the dialog.
     * 
     * @return The {@link EditText} widget that will be shown in the dialog.
     */
    public EditText getEditText()
    {
        return mEditText;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent())
        {
            // No need to save instance state since it's persistent
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }
    
    private static class SavedState extends BaseSavedState
    {
        float value;
        
        public SavedState(Parcel source)
        {
            super(source);
            value = source.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            dest.writeFloat(value);
        }

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>()
				{
        			public SavedState createFromParcel(Parcel in)
        			{
        				return new SavedState(in);
        			}

        			public SavedState[] newArray(int size)
        			{
        				return new SavedState[size];
        			}
				};
    }
}
