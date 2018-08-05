package me.jlurena.ritscheduler;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightonke.boommenu.Util;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import java.io.IOException;

import me.jlurena.ritscheduler.models.Course;
import me.jlurena.ritscheduler.models.Meeting;
import me.jlurena.ritscheduler.models.Term;

public class CourseCardFragment extends Fragment {
    private static final String ARG_PARAM1 = "course";
    private static final String ARG_PARAM2 = "isexisting";
    private static final ObjectMapper mapper = new ObjectMapper();
    private Course course;
    private ImageButton mAddCourseButton;
    private TextView mCourseSection;
    private TextView mCourseName;
    private TextView mCourseTerm;
    private GridLayout mCourseDetailsLayout;
    private ConstraintLayout mCourseHeader;
    private ImageView mProfessorIcon;
    private ImageView mCalendarIcon;
    private ImageView mLocationIcon;
    private OnAddCourseClickListener onAddCourseClickListener;
    private ColorSeekBar mColorSlider;
    private int currentColor;

    /**
     * Factory method to create an instance of CardFragment.
     *
     * @param context Application context.
     * @param course The course object.
     * @param isSavedCourse Flag indicating if its a saved course.
     * @return A new instance of fragment CourseCardFragment.
     */
    public static CourseCardFragment newInstance(Context context, Course course, boolean isSavedCourse) {
        CourseCardFragment fragment = new CourseCardFragment();
        Bundle args = new Bundle();
        try {
            args.putString(ARG_PARAM1, mapper.writeValueAsString(course));
            args.putBoolean(ARG_PARAM2, isSavedCourse);
            fragment.setArguments(args);
        } catch (JsonProcessingException e) {
            Utils.alertDialogFactory(context, R.string.error, context.getString(R.string.generic_error)).show();
        }
        return fragment;
    }

    private TextView createTextView(String text, int row, int col) {
        TextView textView = new TextView(getActivity());
        GridLayout.LayoutParams gllp = new GridLayout.LayoutParams();

        gllp.rowSpec = GridLayout.spec(row);
        gllp.columnSpec = GridLayout.spec(col);
        gllp.setMarginStart(Util.dp2px(10));
        gllp.setMarginEnd(Util.dp2px(10));
        textView.setLayoutParams(gllp);

        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.dark_gray));
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    private void initAddCourseButton() {
        final RotateAnimation rotateAnimation = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        final Handler clickHandler = new Handler();

        final Runnable clickRunnable = new Runnable() {
            @Override
            public void run() {
                onAddCourseClickListener.addCourseListener(course);
            }
        };

        // Fade in animation of the check mark.
        // Fade check in, then call clickListener with a delay so user can see it.
        final Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mAddCourseButton.setImageResource(R.drawable.check);
                clickHandler.postDelayed(clickRunnable, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mAddCourseButton.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });


        this.mAddCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onAddCourseClickListener != null) {
                    // The animations are handling the click
                    mAddCourseButton.startAnimation(rotateAnimation);
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void initColorSeekbar() {
        // Create clone of drawables to set on header
        final Drawable headerDrawable =
                getResources().getDrawable(R.drawable.course_view_header_background, null).getConstantState().newDrawable().mutate();
        final Drawable addButtonDrawable =
                getResources().getDrawable(R.drawable.ripple_round_button, null).getConstantState().newDrawable().mutate();

        this.mColorSlider.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
                currentColor = color;
                headerDrawable.setTint(color);
                mCourseHeader.setBackground(headerDrawable);

                addButtonDrawable.setTint(color);
                mAddCourseButton.setBackground(Utils.getPressedColorRippleDrawable(ColorUtils.blendARGB(color, Color.BLACK, 0.2F),
                        addButtonDrawable));

                course.setColor(currentColor);
            }
        });

        this.mColorSlider.setOnInitDoneListener(new ColorSeekBar.OnInitDoneListener() {
            @Override
            public void done() {
                headerDrawable.setTint(currentColor);
                mCourseHeader.setBackground(headerDrawable);

                addButtonDrawable.setTint(currentColor);
                mAddCourseButton.setBackground(Utils.getPressedColorRippleDrawable(ColorUtils.blendARGB(currentColor, Color.BLACK,
                        0.2F), addButtonDrawable));
                mColorSlider.setColorBarPosition(mColorSlider.getColorIndexPosition(currentColor));
            }
        });

    }

    private void initCourseCard() {


        this.mCourseName.setText(this.course.getCourseTitleLong());
        this.mCourseSection.setText(this.course.getQualifiedName());
        this.mCourseTerm.setText(Term.of(this.course.getStartingTerm()).longTermName());
        initCourseCardDetails();
        initColorSeekbar();
        initAddCourseButton();

    }

    private void initCourseCardDetails() {
        final int dayTimesCol = 1;
        final int locationsCol = 3;
        Meeting meetings = this.course.getMeetings();
        String dayTimes[] = meetings.getDayTimes();
        int dayTimesLength = meetings.getDayTimes().length;
        // Sometimes locations < meetings, in this case duplicate this list
        String[] locations = meetings.getLocationsShortForEachDayTime();
        // Set professor name, set icon position
        String professors = meetings.isSameInstructor() ? meetings.getInstructors()[0].trim() : TextUtils.join(", ", meetings.getInstructors());
        GridLayout.LayoutParams glParams = (GridLayout.LayoutParams) this.mProfessorIcon.getLayoutParams();
        glParams.rowSpec = GridLayout.spec(dayTimesLength + 1);
        this.mProfessorIcon.setLayoutParams(glParams);

        TextView tv = createTextView(professors, dayTimesLength + 1, 1);
        this.mCourseDetailsLayout.addView(tv);

        // Set icon span of course meetings icons
        glParams = (GridLayout.LayoutParams) this.mCalendarIcon.getLayoutParams();
        glParams.rowSpec = GridLayout.spec(0, dayTimesLength);
        this.mCalendarIcon.setLayoutParams(glParams);

        // Set icon span of course location icon
        glParams = (GridLayout.LayoutParams) this.mLocationIcon.getLayoutParams();
        glParams.rowSpec = GridLayout.spec(0, dayTimesLength);
        this.mLocationIcon.setLayoutParams(glParams);


        // Set days, times and location
        for (int i = 0; i < dayTimesLength; i++) {
            this.mCourseDetailsLayout.addView(createTextView(dayTimes[i], i, dayTimesCol));
            // Its at bottom, + teacher row + times rows

            this.mCourseDetailsLayout.addView(createTextView(locations[i], i, locationsCol));
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                this.course = mapper.readValue(getArguments().getString(ARG_PARAM1), Course.class);
                if (this.course.getColor() != 0) {
                    this.currentColor = this.course.getColor();
                }
            } catch (IOException e) {
                Utils.alertDialogFactory(getActivity(), R.string.error, getString(R.string.generic_error)).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.course_card, container, false);
        try {
            if (savedInstanceState != null) {
                this.course = mapper.readValue(savedInstanceState.getString(ARG_PARAM1), Course.class);
            }
        } catch (IOException e) {
            Utils.alertDialogFactory(getActivity(), R.string.error, getString(R.string.generic_error)).show();
        }

        this.currentColor = this.course.getColor() != 0 ? this.course.getColor() : getResources().getColor(R.color.color_primary);
        this.mAddCourseButton = view.findViewById(R.id.course_add_fab);
        this.mCourseName = view.findViewById(R.id.course_name_tv);
        this.mCourseSection = view.findViewById(R.id.course_section_tv);
        this.mCourseTerm = view.findViewById(R.id.course_term_tv);
        this.mCourseDetailsLayout = view.findViewById(R.id.course_details_gl);
        this.mCourseHeader = view.findViewById(R.id.course_header_container);
        this.mProfessorIcon = view.findViewById(R.id.course_professor_icon);
        this.mCalendarIcon = view.findViewById(R.id.course_calendar_icon);
        this.mLocationIcon = view.findViewById(R.id.course_location_icon);
        this.mColorSlider = view.findViewById(R.id.colorSlider);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initCourseCard();
    }

    /**
     * Sets onAddCourseClickListener for Add button.
     *
     * @param onAddCourseClickListener The Add click listener.
     */
    public void setOnAddCourseClickListener(OnAddCourseClickListener onAddCourseClickListener) {
        this.onAddCourseClickListener = onAddCourseClickListener;
    }

    public interface OnAddCourseClickListener {
        /**
         * Action to implement when adding a course.
         *
         * @param course Course to add.
         */
        void addCourseListener(Course course);
    }
}
