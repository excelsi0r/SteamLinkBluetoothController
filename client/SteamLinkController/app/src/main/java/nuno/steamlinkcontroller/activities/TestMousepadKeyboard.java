package nuno.steamlinkcontroller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import nuno.steamlinkcontroller.R;
import nuno.steamlinkcontroller.logic.OneFingerFSM;
import nuno.steamlinkcontroller.logic.OneFingerState;
import nuno.steamlinkcontroller.logic.TwoFingerFSM;
import nuno.steamlinkcontroller.logic.TwoFingerState;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.MotionEvent.*;

public class TestMousepadKeyboard extends AppCompatActivity
{
    private int MAX_DELTA;
    private final int DELAY = 10;

    //one finger mousepad
    private OneFingerFSM oneFingerFSM = new OneFingerFSM();
    private boolean drag1 = false;

    //two finger mousepad
    private TwoFingerFSM twoFingerFSM = new TwoFingerFSM();
    private boolean drag2 = false;

    //mouse up and down
    private boolean upMouse = false;
    private boolean downMouse = false;

    //Views for keyboard
    EditText dummyText;

    @Override
    protected void onPause()
    {
        hideKeyboard();
        super.onPause();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mousepad_keyboard);

        final ImageView img = findViewById(R.id.imageView);
        final Button leftButton = findViewById(R.id.left_mouse);
        final Button rightButton = findViewById(R.id.right_mouse);
        final Button upButton = findViewById(R.id.mouseUp);
        final Button downButton = findViewById(R.id.mouseDown);
        final ImageButton keyboardButton = findViewById(R.id.keyboardButton);
        dummyText = findViewById(R.id.dummyText);
        final Button f1Button = findViewById(R.id.f1);
        final Button f2Button = findViewById(R.id.f2);
        final Button f3Button = findViewById(R.id.f3);
        final Button f4Button = findViewById(R.id.f4);
        final Button f5Button = findViewById(R.id.f5);
        final Button f6Button = findViewById(R.id.f6);
        final Button f7Button = findViewById(R.id.f7);
        final Button f8Button = findViewById(R.id.f8);
        final Button f9Button = findViewById(R.id.f9);
        final Button escButton = findViewById(R.id.escButton);
        final Button homeButton = findViewById(R.id.homeButton);
        final Button pgUpButton = findViewById(R.id.pgUpButton);
        final Button delButton = findViewById(R.id.delButton);
        final Button tabButton = findViewById(R.id.tabButton);
        final Button endButton = findViewById(R.id.endButton);
        final Button pgDownButton = findViewById(R.id.pgDownButton);
        final Button insButton = findViewById(R.id.insButton);
        final Button ctrlButton = findViewById(R.id.ctrlButton);
        final ImageButton winButton = findViewById(R.id.windowsButton);
        final Button altButton = findViewById(R.id.altButton);
        final Button shiftButton = findViewById(R.id.shiftButton);
        final Button arrowUpButton = findViewById(R.id.arrowUp);
        final Button arrowDownButton = findViewById(R.id.arrowDown);
        final Button arrowLeftButton = findViewById(R.id.arrowLeft);
        final Button arrowRightButton = findViewById(R.id.arrowRight);

        MAX_DELTA = getResources().getInteger(R.integer.MAX_DELTA);

        final Handler handler=new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                final long curr = System.currentTimeMillis();

                OneFingerState state = oneFingerFSM.getEventToSend();
                switch (state)
                {
                    case FU1:
                        mouseOneFingerOneTap();
                        break;
                    case SD1:
                        mouseOneFingerDrag();
                        drag1 = true;
                        break;
                    case SU1:
                        mouseOneFingerDoubleTap();
                }

                TwoFingerState state2 = twoFingerFSM.getEventToSend();
                switch (state2)
                {
                    case FD2:
                        drag2 = true;
                        mouseTwoFingerDrag();
                        break;
                    case FU2:
                        mouseTwoFingerOneTap();
                        break;
                }


                if(upMouse)
                {
                    btnMouseWheelUp();
                }

                if(downMouse)
                {
                    btnMouseWheelDown();
                }

                handler.postDelayed(this,DELAY);
            }
        });

        img.setOnTouchListener(new View.OnTouchListener()
        {
            int posX = 0;
            int posY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                long curr = System.currentTimeMillis();

                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        oneFingerFSM.updateState(true);
                        break;

                    case (ACTION_UP):
                        if(drag1)
                        {
                            drag1 = false;
                            mouseOneFingerRelease();
                        }
                        else
                            oneFingerFSM.updateState(false);

                        break;

                    case (ACTION_MOVE):
                        int diffX = (int) motionEvent.getX() - posX;
                        int diffY = (int) motionEvent.getY() - posY;

                        if(motionEvent.getPointerCount() == 1 && (diffX != 0 || diffY != 0))
                        {
                            mouseOneFingerMovement(diffX, diffY);
                        }
                        else if(motionEvent.getPointerCount() > 1 && diffY != 0)
                        {
                            mouseTwoFingerMovement(diffY);
                        }

                        break;
                    case (ACTION_POINTER_DOWN):
                        twoFingerFSM.updateState(true);
                        break;
                    case(ACTION_POINTER_UP):
                        if(drag2)
                        {
                            drag2 = false;
                            mouseTwoFingerRelease();
                        }
                        else
                            twoFingerFSM.updateState(false);
                        break;
                }

                posX = (int) motionEvent.getX();
                posY = (int) motionEvent.getY();

                return true;
            }
        });


        leftButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        btnMouseLeftDown();
                        break;

                    case (ACTION_UP):
                        btnMouseLeftUp();
                        break;
                }
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        btnMouseRightDown();
                        break;

                    case (ACTION_UP):
                        btnMouseRightUp();

                        break;
                }

                return true;
            }
        });

        upButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        upMouse = true;
                        btnMouseUpDown();
                        break;

                    case (ACTION_UP):
                        upMouse = false;
                        btnMouseUpUp();
                        break;
                }
                return false;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        downMouse = true;
                        btnMouseDownDown();
                        break;

                    case (ACTION_UP):
                        downMouse = false;
                        btnMouseDownUp();
                        break;
                }

                return false;
            }
        });

        keyboardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showKeyboard();
            }
        });

        dummyText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(count > before && count == 1)
                    key(charSequence.charAt(start));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        dummyText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent)
            {
                switch (keyEvent.getKeyCode())
                {
                    case KEYCODE_DEL:
                        if(keyEvent.getAction() == ACTION_DOWN)
                            keyBackspace(true);
                        else
                            keyBackspace(false);
                }
                return false;
            }
        });

        dummyText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_PREVIOUS))
                    keyEnter();
                return false;
            }
        });

        f1Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf1(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf1(false);
                return true;
            }
        });

        f2Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf2(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf2(false);
                return true;
            }
        });

        f3Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf3(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf3(false);
                return true;
            }
        });

        f4Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf4(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf4(false);
                return true;
            }
        });

        f5Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf5(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf5(false);
                return true;
            }
        });

        f6Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf6(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf6(false);
                return true;
            }
        });

        f7Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf7(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf7(false);
                return true;
            }
        });

        f8Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf8(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf8(false);
                return true;
            }
        });

        f9Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyf9(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyf9(false);
                return true;
            }
        });

        escButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyEsc(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyEsc(false);
                return true;
            }
        });

        pgUpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyPgUp(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyPgUp(false);
                return true;
            }
        });

        delButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyDel(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyDel(false);
                return true;
            }
        });

        delButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyDel(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyDel(false);
                return true;
            }
        });

        tabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyTab(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyTab(false);
                return true;
            }
        });

        endButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyEnd(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyEnd(false);
                return true;
            }
        });

        pgDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyPgDown(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyPgDown(false);
                return true;
            }
        });

        insButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyIns(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyIns(false);
                return true;
            }
        });

        ctrlButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyCtrl(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyCtrl(false);
                return true;
            }
        });

        winButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyWin(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyWin(false);
                return true;
            }
        });

        altButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyAlt(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyAlt(false);
                return true;
            }
        });

        shiftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyShift(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyShift(false);
                return true;
            }
        });

        arrowUpButton.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowUp(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowUp(false);
                return true;
            }
        });


        arrowDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowDown(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowDown(false);
                return true;
            }
        });


        arrowLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowLeft(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowLeft(false);
                return true;
            }
        });


        arrowRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if((motionEvent.getAction() & ACTION_MASK) == ACTION_DOWN)
                    keyArrowRight(true);
                else if((motionEvent.getAction() & ACTION_MASK) == ACTION_UP)
                    keyArrowRight(false);
                return true;
            }
        });


    }

    public void toast()
    {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
    }

    /**
       19 different events
     */

    private void mouseOneFingerOneTap()
    {
        //TODO send down, up
        Log.d("EVENTCODE", 1 + "");
    }

    private void mouseOneFingerDrag()
    {
        //TODO send down
        Log.d("EVENTCODE", 2 + "");
    }

    private void mouseOneFingerRelease()
    {
        //TODO send up
        Log.d("EVENTCODE", 3 + "");
    }

    private void mouseOneFingerDoubleTap()
    {
        //TODO send down, up, down, up
        Log.d("EVENTCODE",  4 + "");
    }

    private void mouseOneFingerMovement(int x, int y)
    {
        //TODO send move x and y
        Log.d("EVENTCODE", 5 + "");
    }

    private void mouseTwoFingerOneTap()
    {
        //TODO send two finger down, up
        Log.d("EVENTCODE", 6 + "");
    }

    private void mouseTwoFingerDrag()
    {
        //TODO send two finger down
        Log.d("EVENTCODE", 7 + "");
    }

    private void mouseTwoFingerRelease()
    {
        //TODO send two finger up
        Log.d("EVENTCODE", 8 + "");
    }

    private void mouseTwoFingerMovement(int y)
    {
        //TODO send rel wheel y event
        Log.d("EVENTCODE", 9+ "");
    }

    private void btnMouseWheelUp()
    {
        //TODO send rel vertical mouse up
        Log.d("EVENTCODE", 10 + "");
    }

    private void btnMouseWheelDown()
    {
        //TODO send rel vertical mouse down
        Log.d("EVENTCODE", 11 + "");
    }

    private void btnMouseLeftDown()
    {
        //TODO send left mouse down
        Log.d("EVENTCODE", 12 + "");
    }

    private void btnMouseLeftUp()
    {
        //TODO send left mouse up
        Log.d("EVENTCODE", 13 + "");
    }

    private void btnMouseRightDown()
    {
        //TODO send right mouse down
        Log.d("EVENTCODE", 14 + "");
    }

    private void btnMouseRightUp()
    {
        //TODO send right mouse up
        Log.d("EVENTCODE", 15 + "");
    }

    private void btnMouseUpDown()
    {
        //TODO send up mouse down
        Log.d("EVENTCODE", 16 + "");
    }

    private void btnMouseUpUp()
    {
        //TODO send up mouse up
        Log.d("EVENTCODE", 17 + "");
    }

    private void btnMouseDownDown()
    {
        //TODO send down mouse down
        Log.d("EVENTCODE", 18 + "");
    }

    private void btnMouseDownUp()
    {
        //TODO send down mouse up
        Log.d("EVENTCODE", 19 + "");
    }

    private void keyEnter()
    {
        //TODO send enter down, up
        Log.d("EVENTCODE", 20 + "");
    }

    private void keyBackspace(boolean down)
    {
        if(down)
        {
            //TODO send backspace down
            Log.d("EVENTCODE", 21 + "");
        }
        else
        {
            //TODO send backspace up
            Log.d("EVENTCODE", 22 + "");
        }
    }

    private void key(char key)
    {
        //TODO map key to linux integer and send
        Log.d("EVENTCODE", 23 + " '" + key + "'");
    }

    private void keyf1(boolean down)
    {
        if(down) {
            //TODO send f1 down
            Log.d("EVENTCODE", 24 + ""); }
        else {
            //TODO send f1 up
            Log.d("EVENTCODE", 25 + ""); }
    }

    private void keyf2(boolean down)
    {
        if(down) {
            //TODO send f2 down
            Log.d("EVENTCODE", 26 + ""); }
        else {
            //TODO send f2 up
            Log.d("EVENTCODE", 27 + ""); }
    }

    private void keyf3(boolean down)
    {
        if(down) {
            //TODO send f3 down
            Log.d("EVENTCODE", 28 + ""); }
        else {
            //TODO send f3 up
            Log.d("EVENTCODE", 29 + ""); }
    }


    private void keyf4(boolean down)
    {
        if(down) {
            //TODO send f4 down
            Log.d("EVENTCODE", 30 + ""); }
        else {
            //TODO send f4 up
            Log.d("EVENTCODE", 31 + ""); }
    }

    private void keyf5(boolean down)
    {
        if(down) {
            //TODO send f5 down
            Log.d("EVENTCODE", 32 + ""); }
        else {
            //TODO send f5 up
            Log.d("EVENTCODE", 33 + ""); }
    }


    private void keyf6(boolean down)
    {
        if(down) {
            //TODO send f6 down
            Log.d("EVENTCODE", 34 + ""); }
        else {
            //TODO send f6 up
            Log.d("EVENTCODE", 35 + ""); }
    }

    private void keyf7(boolean down)
    {
        if(down) {
            //TODO send f7 down
            Log.d("EVENTCODE", 36 + ""); }
        else {
            //TODO send f7 up
            Log.d("EVENTCODE", 37 + ""); }
    }

    private void keyf8(boolean down)
    {
        if(down) {
            //TODO send f8 down
            Log.d("EVENTCODE", 38 + ""); }
        else {
            //TODO send f8 up
            Log.d("EVENTCODE", 39 + ""); }
    }

    private void keyf9(boolean down)
    {
        if(down) {
            //TODO send f9 down
            Log.d("EVENTCODE", 40 + ""); }
        else {
            //TODO send f9 up
            Log.d("EVENTCODE", 41 + ""); }
    }

    private void keyEsc(boolean down)
    {
        if(down) {
            //TODO send esc down
            Log.d("EVENTCODE", 42 + ""); }
        else {
            //TODO send esc up
            Log.d("EVENTCODE", 43 + ""); }
    }
    private void keyHome(boolean down)
    {
        if(down) {
            //TODO send home down
            Log.d("EVENTCODE", 44 + ""); }
        else {
            //TODO send home up
            Log.d("EVENTCODE", 45 + ""); }
    }

    private void keyPgUp(boolean down)
    {
        if(down) {
            //TODO send PgUp down
            Log.d("EVENTCODE", 46 + ""); }
        else {
            //TODO send PgUp up
            Log.d("EVENTCODE", 47 + ""); }
    }

    private void keyPgDown(boolean down)
    {
        if(down) {
            //TODO send PgDn down
            Log.d("EVENTCODE", 48 + ""); }
        else {
            //TODO send PgDn up
            Log.d("EVENTCODE", 49 + ""); }
    }

    private void keyDel(boolean down)
    {
        if(down) {
            //TODO send del down
            Log.d("EVENTCODE", 50 + ""); }
        else {
            //TODO send del up
            Log.d("EVENTCODE", 51 + ""); }
    }

    private void keyTab(boolean down)
    {
        if(down) {
            //TODO send tab down
            Log.d("EVENTCODE", 52 + ""); }
        else {
            //TODO send tab up
            Log.d("EVENTCODE", 53 + ""); }
    }

    private void keyEnd(boolean down)
    {
        if(down) {
            //TODO send end down
            Log.d("EVENTCODE", 54 + ""); }
        else {
            //TODO send end up
            Log.d("EVENTCODE", 55 + ""); }
    }

    private void keyIns(boolean down)
    {
        if(down) {
            //TODO send ins down
            Log.d("EVENTCODE", 56 + ""); }
        else {
            //TODO send ins up
            Log.d("EVENTCODE", 57 + ""); }
    }

    private void keyCtrl(boolean down)
    {
        if(down) {
            //TODO send ctrl down
            Log.d("EVENTCODE", 58 + ""); }
        else {
            //TODO send ctrl up
            Log.d("EVENTCODE", 59 + ""); }
    }

    private void keyWin(boolean down)
    {
        if(down) {
            //TODO send win down
            Log.d("EVENTCODE", 60 + ""); }
        else {
            //TODO send win up
            Log.d("EVENTCODE", 61 + ""); }
    }

    private void keyAlt(boolean down)
    {
        if(down) {
            //TODO send alt down
            Log.d("EVENTCODE", 62 + ""); }
        else {
            //TODO send alt up
            Log.d("EVENTCODE", 63 + ""); }
    }

    private void keyShift(boolean down)
    {
        if(down) {
            //TODO send shift down
            Log.d("EVENTCODE", 64 + ""); }
        else {
            //TODO send shift up
            Log.d("EVENTCODE", 65 + ""); }
    }

    private void keyArrowUp(boolean down)
    {
        if(down) {
            //TODO send arrowUp down
            Log.d("EVENTCODE", 66 + ""); }
        else {
            //TODO send arrowUp up
            Log.d("EVENTCODE", 67 + ""); }
    }

    private void keyArrowDown(boolean down)
    {
        if(down) {
            //TODO send arrowDown down
            Log.d("EVENTCODE", 68 + ""); }
        else {
            //TODO send arrowDown up
            Log.d("EVENTCODE", 69 + ""); }
    }

    private void keyArrowLeft(boolean down)
    {
        if(down) {
            //TODO send arrowLeft down
            Log.d("EVENTCODE", 70 + ""); }
        else {
            //TODO send arrowLeft up
            Log.d("EVENTCODE", 71 + ""); }
    }

    private void keyArrowRight(boolean down)
    {
        if(down) {
            //TODO send arrowLeft down
            Log.d("EVENTCODE", 72 + ""); }
        else {
            //TODO send arrowLeft up
            Log.d("EVENTCODE", 73 + ""); }
    }

    private void showKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(dummyText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard()
    {

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(dummyText.getWindowToken(), 0);

    }
}
