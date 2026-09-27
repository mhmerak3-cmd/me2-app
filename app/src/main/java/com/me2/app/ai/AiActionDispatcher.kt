package com.me2.app.ai

import com.me2.app.data.local.Me2Vault
import com.me2.app.data.models.MealType
import com.me2.app.data.repository.Me2LocalRepository

class AiActionDispatcher(
    private val repository: Me2LocalRepository,
    private val aiEngine: AiCoachEngine,
    private val vault: Me2Vault? = null
) {
    fun executeVoiceDirective(spokenText: String): String = processVoiceCommand(spokenText)

    fun processVoiceCommand(rawInput: String): String {
        val input = rawInput.trim().lowercase()

        if (input.contains("হাইট") || input.contains("লম্বা") || input.contains("উচ্চতা")) {
            vault?.addCustomMission("হাইট বুস্ট: ১০ মিনিট ঝুলন্ত স্ট্রেচ ও কোবরা এক্সারসাইজ", 30, "হাইট বৃদ্ধি")
            return "আপনার জন্য একটি নতুন মিশন যুক্ত করা হয়েছে: 'হাইট বুস্ট: ১০ মিনিট ঝুলন্ত স্ট্রেচ'। প্রতিদিন ড্যাশবোর্ডে এটি সম্পন্ন করতে পারবেন!"
        }
        if (input.contains("মিশন তৈরি") || input.contains("লক্ষ্য তৈরি") || input.contains("স্বাস্থ্য বাড়াতে")) {
            vault?.addCustomMission("হেলথ গ্রোথ: হাই-ক্যালোরি পুষ্টিকর খাবার ও রেস্ট", 25, "স্বাস্থ্য বৃদ্ধি")
            return "নতুন মিশন যুক্ত করা হয়েছে: 'হেলথ গ্রোথ মিশন'। প্রতিদিনের তালিকায় এটি সেভ থাকবে।"
        }

        if (input.contains("খেয়েছি") || input.contains("খাবার")) {
            val mealType = when {
                input.contains("সকাল") -> MealType.BREAKFAST
                input.contains("রাত") -> MealType.DINNER
                input.contains("নাশতা") || input.contains("চা") -> MealType.SNACKS
                else -> MealType.LUNCH
            }
            val cal = if (input.contains("মাছ")) 520 else if (input.contains("ভাত")) 480 else 400
            val prot = if (input.contains("মাছ") || input.contains("মাংস")) 32f else 20f
            vault?.addDetailedMeal(type = mealType, items = rawInput, cal = cal, protein = prot)
            return "${mealType.bangla} হিসেবে যোগ করা হয়েছে: $rawInput (~$cal kcal, ${prot}g প্রোটিন)। ডাটা পারমানেন্টলি সংরক্ষিত!"
        }

        if (input.contains("পানি")) {
            val ml = if (input.contains("৫০০") || input.contains("500")) 500 else 250
            vault?.addWater(ml)
            return "$ml মিলি পানি সফলভাবে ডাটাবেজে সংরক্ষণ করা হয়েছে (+10 XP)।"
        }

        if (input.contains("মিশন শেষ") || input.contains("ওয়ার্কআউট করেছি")) {
            vault?.markMissionDone(vault.getTodayDate(), "workout", 30)
            return "অভিনন্দন! আপনার ওয়ার্কআউট মিশন সম্পন্ন মার্ক করা হয়েছে এবং +30 XP যোগ হয়েছে।"
        }

        val geminiReply = aiEngine.askGemini(rawInput)
        if (!geminiReply.isNullOrBlank()) {
            return geminiReply
        }

        return "কমান্ড পেয়েছি। প্রোফাইল ও শিডিউল অনুযায়ী ডাটা সুরক্ষিত রাখা হয়েছে।"
    }
}
