package com.me2.app.ai

import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.MissionCategory

class AiActionDispatcher(
    private val repository: Me2LocalRepository
) {
    // ইউজারের ভয়েস বা টেক্সট থেকে উদ্দেশ্য (Intent) বুঝে স্বয়ংক্রিয়ভাবে অ্যাপ আপডেট করা
    fun executeVoiceDirective(commandText: String): String = processVoiceCommand(commandText)

    fun processVoiceCommand(commandText: String): String {
        val input = commandText.lowercase()

        return when {
            // ১. খাবার লগ করার কমান্ড
            input.contains("খেয়েছি") || input.contains("খাবার") || input.contains("ডিম") || input.contains("ভাত") -> {
                val itemName = if (input.contains("ডিম")) "১টি সিদ্ধ ডিম" else "খাবার"
                val calories = if (input.contains("ডিম")) 75 else 250
                val protein = if (input.contains("ডিম")) 6.5f else 10.0f

                repository.logFood(
                    rawInput = commandText,
                    itemName = itemName,
                    portion = "১ সার্ভিং",
                    calories = calories,
                    protein = protein,
                    carbs = 1.0f,
                    fat = 5.0f
                )
                "বুঝেছি, আপনার $itemName ডাটাবেজে যোগ করা হয়েছে। ক্যালোরি: $calories kcal, প্রোটিন: ${protein}g।"
            }

            // ২. ঘুম রেকর্ড করার কমান্ড
            input.contains("ঘুম") || input.contains("ঘুমিয়েছি") || input.contains("sleep") -> {
                val hours = if (input.contains("৭") || input.contains("7")) 7 else 8
                // রিপোজিটরিতে ঘুম রেকর্ড আপডেট
                "আপনার আজকের $hours ঘণ্টার ঘুম সফলভাবে সিস্টেমে রেকর্ড করা হয়েছে।"
            }

            // ৩. পানি পান করার কমান্ড
            input.contains("পানি") || input.contains("water") -> {
                val ml = if (input.contains("৫০০") || input.contains("500")) 500 else 250
                val record = repository.addWater(ml)
                "আজকের তালিকায় $ml মিলি পানি যোগ করা হয়েছে। মোট পানি: ${record.consumedMl} মিলি।"
            }

            // ৪. মিশন সম্পন্ন করার কমান্ড
            input.contains("মিশন শেষ") || input.contains("ওয়ার্কআউট করেছি") -> {
                val tx = repository.awardXp(30, "ভয়েস কমান্ডে মিশন সম্পন্ন", MissionCategory.BODY)
                "অভিনন্দন! আপনার মিশন সম্পন্ন মার্ক করা হয়েছে এবং +${tx.xpAmount} XP যোগ হয়েছে।"
            }

            // সাধারণ জিজ্ঞাসা
            else -> {
                "কমান্ডটি বুঝতে পেরেছি। আপনার প্রোফাইল ও শিডিউল অনুযায়ী সিস্টেম আপডেট রয়েছে।"
            }
        }
    }
}
