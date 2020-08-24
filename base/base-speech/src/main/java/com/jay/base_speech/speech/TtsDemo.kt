package com.jay.base_speech.speech

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.iflytek.cloud.*
import com.iflytek.cloud.util.ResourceUtil
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE
import com.iflytek.speech.setting.TtsSettings
import com.jay.base_component.arouter.ARHelper
import com.jay.base_component.arouter.ARPath
import com.jay.base_component.constant.Constants
import com.jay.base_speech.R

@Route(path = ARPath.PathSpeech.SPEECH_ACTIVITY_PATH)
class TtsDemo : Activity(), View.OnClickListener {
    // 语音合成对象
    private var mTts: SpeechSynthesizer? = null

    // 云端发音人列表
    private var cloudVoicersEntries: Array<String>? = null
    private var cloudVoicersValue: Array<String>? = null

    // 本地发音人列表
    private var localVoicersEntries: Array<String>? = null
    private var localVoicersValue: Array<String>? = null

    // 增强版发音人列表
    private var xttsVoicersEntries: Array<String>? = null
    private var xttsVoicersValue: Array<String>? = null

    //缓冲进度
    private var mPercentForBuffering = 0

    //播放进度
    private var mPercentForPlaying = 0

    // 云端/本地选择按钮
    private var mRadioGroup: RadioGroup? = null

    private var ttsText: TextView? = null

    private var ttsTextMain: TextView? = null

    private var llTest: LinearLayout? = null

    private var mTitle: String? = null

    // 引擎类型
    private var mEngineType = SpeechConstant.TYPE_CLOUD
    private var mToast: Toast? = null
    private var mSharedPreferences: SharedPreferences? = null

    @SuppressLint("ShowToast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_speech_tts)
        initIntent()
        initLayout()

        initSpeech()

        initRes()
    }

    private fun initIntent() {
        val mapParams = ARHelper.getParamsMapFromIntentByJson(intent)
        mTitle = ARHelper.getStrFromParamsMap(mapParams, Constants.MapKey.TITLE)
        Log.d(TAG, "mTitle： $mTitle")


    }

    private fun initRes() {
        // 云端发音人名称列表
        cloudVoicersEntries =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_cloud_entries)
        cloudVoicersValue =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_cloud_values)

        // 本地发音人名称列表
        localVoicersEntries =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_local_entries)
        localVoicersValue =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_local_values)

        // 增强版发音人名称列表
        xttsVoicersEntries =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_xtts_entries)
        xttsVoicersValue =
            resources.getStringArray(com.iflytek.mscv5plusdemo.R.array.voicer_xtts_values)
        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    }

    private fun initSpeech() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener)
    }


    /**
     * 初始化Layout。
     */
    private fun initLayout() {
        ttsText = (findViewById<View>(R.id.tts_text) as TextView)
        ttsTextMain = (findViewById<View>(R.id.tts_text_main) as TextView)
        ttsTextMain?.text = mTitle
        llTest = (findViewById<View>(R.id.ll_test) as LinearLayout)
        llTest?.visibility = View.GONE
        ttsTextMain?.setOnClickListener {
            llTest?.visibility = View.VISIBLE
        }
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_play).setOnClickListener(
            this
        )
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_cancel).setOnClickListener(
            this
        )
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_pause).setOnClickListener(
            this
        )
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_resume).setOnClickListener(
            this
        )
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.image_tts_set).setOnClickListener(
            this
        )
        findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_btn_person_select).setOnClickListener(
            this
        )
        mRadioGroup =
            findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_rediogroup) as RadioGroup
        mRadioGroup!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                //在线合成
                R.id.tts_radioCloud -> {
                    mEngineType = SpeechConstant.TYPE_CLOUD
                }
                //本地合成
                R.id.tts_radioLocal -> {
                    mEngineType = SpeechConstant.TYPE_LOCAL
                }
                //增强版合成
                R.id.tts_radioXtts -> {
                    mEngineType = SpeechConstant.TYPE_XTTS
                }
            }
        }
    }

    override fun onClick(view: View) {
        if (null == mTts) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showTip("创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化")
            return
        }
        val id = view.id
        //
        if (id == com.iflytek.mscv5plusdemo.R.id.image_tts_set) {
            val intent = Intent(this@TtsDemo, TtsSettings::class.java)
            startActivity(intent)
            // 开始合成
            // 收到onCompleted 回调时，合成结束、生成合成音频
            // 合成的音频格式：只支持pcm格式
        } else if (id == com.iflytek.mscv5plusdemo.R.id.tts_play) {
            startSpeech(ttsText?.text?.trim().toString())
            // 取消合成
        } else if (id == com.iflytek.mscv5plusdemo.R.id.tts_cancel) {
            mTts!!.stopSpeaking()
            // 暂停播放
        } else if (id == com.iflytek.mscv5plusdemo.R.id.tts_pause) {
            mTts!!.pauseSpeaking()
            // 继续播放
        } else if (id == com.iflytek.mscv5plusdemo.R.id.tts_resume) {
            mTts!!.resumeSpeaking()
            // 选择发音人
        } else if (id == com.iflytek.mscv5plusdemo.R.id.tts_btn_person_select) {
            showPresonSelectDialog()
        }
    }

    private fun startSpeech(text: String) {
        // 设置参数
        setParam()
        Log.d(TAG, "准备点击： " + System.currentTimeMillis())
        val code = mTts!!.startSpeaking(text, mTtsListener)
        //			/**
        //			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
        //			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
        //			*/
        //			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
        //			int code = mTts.synthesizeToUri(text, path, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            showTip("语音合成失败,错误码: $code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
        }
    }

    /**
     * 发音人选择。
     */
    private fun showPresonSelectDialog() {
        val checkedRadioButtonId = mRadioGroup!!.checkedRadioButtonId // 选择在线合成
        if (checkedRadioButtonId == com.iflytek.mscv5plusdemo.R.id.tts_radioCloud) {
            AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                .setSingleChoiceItems(
                    cloudVoicersEntries,  // 单选框有几项,各是什么名字
                    selectedNumCloud  // 默认的选项
                ) { dialog, which ->
                    // 点击单选框后的处理
                    // 点击了哪一项
                    voicerCloud = cloudVoicersValue?.get(which) ?: ""
                    if ("catherine" == voicerCloud || "henry" == voicerCloud || "vimary" == voicerCloud) {
                        ttsText?.setText("hello world")
                    } else {

                    }
                    selectedNumCloud = which
                    dialog.dismiss()
                }.show()

            // 选择本地合成
        } else if (checkedRadioButtonId == com.iflytek.mscv5plusdemo.R.id.tts_radioLocal) {
            AlertDialog.Builder(this).setTitle("本地合成发音人选项")
                .setSingleChoiceItems(
                    localVoicersEntries,  // 单选框有几项,各是什么名字
                    selectedNumLocal  // 默认的选项
                ) { dialog, which ->
                    // 点击单选框后的处理
                    // 点击了哪一项
                    voicerLocal =
                        (localVoicersValue?.get(which)
                            ?: if ("catherine" == voicerLocal || "henry" == voicerLocal || "vimary" == voicerLocal) {
                                (findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_text) as EditText).setText(
                                    com.iflytek.mscv5plusdemo.R.string.text_tts_source_en
                                )
                            } else {
                                (findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_text) as EditText).setText(
                                    com.iflytek.mscv5plusdemo.R.string.text_tts_source
                                )
                            }) as String
                    selectedNumLocal = which
                    dialog.dismiss()
                }.show()
        } else if (checkedRadioButtonId == com.iflytek.mscv5plusdemo.R.id.tts_radioXtts) {
            AlertDialog.Builder(this).setTitle("增强版合成发音人选项")
                .setSingleChoiceItems(
                    xttsVoicersEntries,  // 单选框有几项,各是什么名字
                    selectedNumLocal  // 默认的选项
                ) { dialog, which ->
                    // 点击单选框后的处理
                    // 点击了哪一项
                    voicerXtts =
                        xttsVoicersValue?.get(which) ?: ""
                    //Toast.makeText(this,voicerXtts,Toast.LENGTH_LONG);
                    println("sssssss:$voicerXtts")
                    if ("catherine" == voicerXtts || "henry" == voicerXtts || "vimary" == voicerXtts) {
                        (findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_text) as EditText).setText(
                            com.iflytek.mscv5plusdemo.R.string.text_tts_source_en
                        )
                    } else {
                        (findViewById<View>(com.iflytek.mscv5plusdemo.R.id.tts_text) as EditText).setText(
                            com.iflytek.mscv5plusdemo.R.string.text_tts_source
                        )
                    }
                    selectedNumLocal = which
                    dialog.dismiss()
                }.show()
        }
    }

    /**
     * 初始化监听。
     */
    private val mTtsInitListener = InitListener { code ->
        Log.d(TAG, "InitListener init() code = $code")
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：$code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
        } else {
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
            startSpeech(ttsTextMain?.text?.trim().toString())

        }
    }

    /**
     * 合成回调监听。
     */
    private val mTtsListener: SynthesizerListener = object : SynthesizerListener {
        override fun onSpeakBegin() {
            //showTip("开始播放");
            Log.d(
                TAG,
                "开始播放：" + System.currentTimeMillis()
            )
        }

        override fun onSpeakPaused() {
            showTip("暂停播放")
        }

        override fun onSpeakResumed() {
            showTip("继续播放")
        }

        override fun onBufferProgress(
            percent: Int, beginPos: Int, endPos: Int,
            info: String
        ) {
            Log.d(TAG, "onBufferProgress,percent: ] =${percent}")

            // 合成进度
            mPercentForBuffering = percent
            showTip(
                String.format(
                    getString(com.iflytek.mscv5plusdemo.R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying
                )
            )
        }

        override fun onSpeakProgress(
            percent: Int,
            beginPos: Int,
            endPos: Int
        ) {
            // 播放进度
            mPercentForPlaying = percent
            showTip(
                String.format(
                    getString(com.iflytek.mscv5plusdemo.R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying
                )
            )
            val process = String.format(
                getString(com.iflytek.mscv5plusdemo.R.string.tts_toast_format),
                mPercentForBuffering, mPercentForPlaying
            )

            if(mPercentForPlaying >= 97){
                this@TtsDemo.finish()
            }

            Log.d(TAG, "onSpeakProgress,process: ] =${process}")

        }

        override fun onCompleted(error: SpeechError) {
            Log.d(TAG, "onCompleted, error ] =${error.errorDescription}")
            if (error == null) {
//                showTip("播放完成")
                this@TtsDemo.finish()
            } else if (error != null) {
                showTip(error.getPlainDescription(true))
            }
        }

        override fun onEvent(
            eventType: Int,
            arg1: Int,
            arg2: Int,
            obj: Bundle?
        ) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                val sid = obj?.getString(SpeechEvent.KEY_EVENT_AUDIO_URL)
                Log.d(TAG, "session id =$sid")
            }

            //实时音频流输出参考
            /*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
            byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
            Log.e("MscSpeechLog", "buf is =" + buf);
        }*/
        }
    }

    private fun showTip(str: String) {
        runOnUiThread {
            mToast!!.setText(str)
//            mToast!!.show()
        }
    }

    /**
     * 参数设置
     */
    private fun setParam() {
        // 清空参数
        mTts!!.setParameter(SpeechConstant.PARAMS, null)
        //设置合成
        if (mEngineType == SpeechConstant.TYPE_CLOUD) {
            //设置使用云端引擎
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            //设置发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicerCloud)
        } else if (mEngineType == SpeechConstant.TYPE_LOCAL) {
            //设置使用本地引擎
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            //设置发音人资源路径
            mTts!!.setParameter(ResourceUtil.TTS_RES_PATH, resourcePath)
            //设置发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicerLocal)
        } else {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_XTTS)
            //设置发音人资源路径
            mTts!!.setParameter(ResourceUtil.TTS_RES_PATH, resourcePath)
            //设置发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicerXtts)
        }
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速
        mTts!!.setParameter(
            SpeechConstant.SPEED,
            mSharedPreferences!!.getString("speed_preference", "50")
        )
        //设置合成音调
        mTts!!.setParameter(
            SpeechConstant.PITCH,
            mSharedPreferences!!.getString("pitch_preference", "50")
        )
        //设置合成音量
        mTts!!.setParameter(
            SpeechConstant.VOLUME,
            mSharedPreferences!!.getString("volume_preference", "50")
        )
        //设置播放器音频流类型
        mTts!!.setParameter(
            SpeechConstant.STREAM_TYPE,
            mSharedPreferences!!.getString("stream_preference", "3")
        )
        //	mTts.setParameter(SpeechConstant.STREAM_TYPE, AudioManager.STREAM_MUSIC+"");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts!!.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mTts!!.setParameter(
            SpeechConstant.TTS_AUDIO_PATH,
            Environment.getExternalStorageDirectory().toString() + "/msc/tts.wav"
        )
    }//合成通用资源
    //发音人资源

    //获取发音人资源路径
    private val resourcePath: String
        private get() {
            val tempBuffer = StringBuffer()
            var type = "tts"
            if (mEngineType == SpeechConstant.TYPE_XTTS) {
                type = "xtts"
            }
            //合成通用资源
            tempBuffer.append(
                ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets, "$type/common.jet")
            )
            tempBuffer.append(";")
            //发音人资源
            if (mEngineType == SpeechConstant.TYPE_XTTS) {
                tempBuffer.append(
                    ResourceUtil.generateResourcePath(
                        this,
                        RESOURCE_TYPE.assets,
                        "$type/$voicerXtts.jet"
                    )
                )
            } else {
                tempBuffer.append(
                    ResourceUtil.generateResourcePath(
                        this,
                        RESOURCE_TYPE.assets,
                        "$type/$voicerLocal.jet"
                    )
                )
            }
            return tempBuffer.toString()
        }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mTts) {
            mTts?.stopSpeaking()
            // 退出时释放连接
            mTts?.destroy()
        }
    }

    companion object {
        private val TAG = TtsDemo::class.java.simpleName

        // 默认云端发音人
        var voicerCloud = "xiaoyan"

        // 默认本地发音人
        var voicerLocal = "xiaoyan"
        var voicerXtts = "xiaoyan"
        private var selectedNumCloud = 0
        private var selectedNumLocal = 0
    }
}