/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel


/**
 * ViewModel containing all the logic needed to run the game
 */
private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)
class GameViewModel : ViewModel() {

    companion object{

        //when game is over
        const val DONE = 0L
        //millisseconds in a second
        const val ONE_SECOND = 1000L
        private const val COUNTDOWN_PANIC_SECONDS = 10L
        //total game time
        const val COUNTDOWN_TIME = 30000L

    }
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    private val timer: CountDownTimer
    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
    get() = _word


    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
    get() = _score

    private val _eventGameFinished = MutableLiveData<Boolean>()
    val eventGameFinished: LiveData<Boolean>
    get() = _eventGameFinished


    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
    get() = _currentTime

    val currentTimeString = Transformations.map(currentTime, {time ->
        DateUtils.formatElapsedTime(time)
    })




    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>


    init {
        _eventGameFinished.value=false
        resetList()
        nextWord()
        _score.value = 0
        //Log.i("GameViewModel", "GameViewModel created!")

        timer = object :CountDownTimer(COUNTDOWN_TIME, ONE_SECOND){
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _eventBuzz.value = BuzzType.GAME_OVER
                _eventGameFinished.value = true
            }

        }
        timer.start()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }


    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
           resetList()
        }
        _word.value = wordList.removeAt(0)


    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value= (score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value= (score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextWord()
    }

    //eventGameFinished should occur once, fragment recreation shouldn't trigger it again. set its value to false
    fun gameFinishedComplete(){
        _eventGameFinished.value = false
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Log.i("GameViewModel", "GameViewModel destroyed!")
    }


}