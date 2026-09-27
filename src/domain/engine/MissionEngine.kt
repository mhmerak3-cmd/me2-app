package com.me2.app.domain.engine

import java.time.LocalDate
import java.time.LocalTime

enum class MissionCategory {
    BODY, NUTRITION, SLEEP, MIND, DISCIPLINE, APPEARANCE, PRODUCTIVITY
}

enum class MissionDifficulty(val rewardXp: Int) {
    EASY(10),
    MEDIUM(25),
    HARD(50),
    EPIC(100)
}

data class MissionItem(
    val id: Long,
    val title: String,
    val description: String,
    val category: MissionCategory,
    val difficulty: MissionDifficulty,
    val estimatedMinutes: Int,
    val isShiftSafe: Boolean,
    val isCoreMission: Boolean,
    val isCompleted: Boolean = false
)

class MissionEngine {

    // দৈনিক মিশনের তালিকা তৈরি
    fun generateDailyMissions(
        date: LocalDate,
        shift: ShiftConfig?,
        isRecoveryMode: Boolean,
        goal: PrimaryGoal
    ): List<MissionItem> {
        val missions = mutableListOf<MissionItem>()
        var idCounter = 1L

        // রিকভারি মোড সক্রিয় থাকলে শুধুমাত্র ৩টি কোর মিশন দেওয়া হবে
        if (isRecoveryMode) {
            missions.add(
                MissionItem(
                    id = idCounter++,
                    title = "হাইড্রেশন বেসলাইন",
                    description = "সারাদিনে ন্যূনতম ২ লিটার পানি পান করুন।",
                    category = MissionCategory.NUTRITION,
                    difficulty = MissionDifficulty.EASY,
                    estimatedMinutes = 1,
                    isShiftSafe = true,
                    isCoreMission = true
                )
            )
            missions.add(
                MissionItem(
                    id = idCounter++,
                    title = "১০ মিনিট হালকা মুভমেন্ট",
                    description = "ক্লান্তি কাটাতে হালকা স্ট্রেচ বা ফ্রি-হ্যান্ড এক্সারসাইজ।",
                    category = MissionCategory.BODY,
                    difficulty = MissionDifficulty.EASY,
                    estimatedMinutes = 10,
                    isShiftSafe = true,
                    isCoreMission = true
                )
            )
            missions.add(
                MissionItem(
                    id = idCounter++,
                    title = "টাইমলি স্লিপ প্রিপারেশন",
                    description = "নির্ধারিত সময়ের ৩০ মিনিট আগে ফোন বন্ধ করে ঘুমানোর প্রস্তুতি নিন।",
                    category = MissionCategory.SLEEP,
                    difficulty = MissionDifficulty.EASY,
                    estimatedMinutes = 30,
                    isShiftSafe = false,
                    isCoreMission = true
                )
            )
            return missions
        }

        // স্ট্যান্ডার্ড মিশন সেট: কোর মিশনসমূহ
        missions.add(
            MissionItem(
                id = idCounter++,
                title = "সকালের হাইড্রেশন ও ফ্রেশ হওয়া",
                description = "ঘুম থেকে উঠে ৫০০ মিলি পানি পান এবং মুখ ধুয়ে প্রস্তুত হওয়া।",
                category = MissionCategory.DISCIPLINE,
                difficulty = MissionDifficulty.EASY,
                estimatedMinutes = 5,
                isShiftSafe = false,
                isCoreMission = true
            )
        )

        // গোল অনুযায়ী মূল ওয়ার্কআউট মিশন
        val workoutTitle = when (goal) {
            PrimaryGoal.BUILD_MUSCLE, PrimaryGoal.IMPROVE_STRENGTH -> "স্ট্রেন্থ ট্রেইনিং সেশন"
            PrimaryGoal.FITNESS, PrimaryGoal.ENDURANCE -> "ফুল বডি কন্ডিশনিং ও কার্ডিও"
            else -> "মোবিলিটি ও কোর এক্সারসাইজ"
        }

        missions.add(
            MissionItem(
                id = idCounter++,
                title = workoutTitle,
                description = "আজকের নির্ধারিত মেইন ওয়ার্কআউট সেশন সম্পন্ন করুন।",
                category = MissionCategory.BODY,
                difficulty = MissionDifficulty.HARD,
                estimatedMinutes = 45,
                isShiftSafe = false,
                isCoreMission = true
            )
        )

        // প্রোটিন ও মিল ট্র্যাকিং
        missions.add(
            MissionItem(
                id = idCounter++,
                title = "দৈনিক প্রোটিন টার্গেট পূরণ",
                description = "সারাদিনের খাবারের হিসাব রাখুন এবং প্রোটিন টার্গেটে পৌঁছান।",
                category = MissionCategory.NUTRITION,
                difficulty = MissionDifficulty.MEDIUM,
                estimatedMinutes = 5,
                isShiftSafe = true,
                isCoreMission = true
            )
        )

        // শিফট চলাকালে সহজে পালনযোগ্য মিশন (Shift-Safe)
        if (shift != null && shift.shiftType != ShiftType.REST_DAY) {
            missions.add(
                MissionItem(
                    id = idCounter++,
                    title = "ডিউটি হাইড্রেশন চেক",
                    description = "কাজের ফাঁকে পানি পান করে হাইড্রেটেড থাকুন।",
                    category = MissionCategory.NUTRITION,
                    difficulty = MissionDifficulty.EASY,
                    estimatedMinutes = 2,
                    isShiftSafe = true,
                    isCoreMission = false
                )
            )
            missions.add(
                MissionItem(
                    id = idCounter++,
                    title = "পোস্টচার ও ডিকম্প্রেশন",
                    description = "টানা কাজের ক্লান্তি কাটাতে কাঁধ ও পিঠের ২ মিনিটের হালকা স্ট্রেচ।",
                    category = MissionCategory.BODY,
                    difficulty = MissionDifficulty.EASY,
                    estimatedMinutes = 3,
                    isShiftSafe = true,
                    isCoreMission = false
                )
            )
        }

        // স্কিন ও গ্রুমিং
        missions.add(
            MissionItem(
                id = idCounter++,
                title = "স্কিন কেয়ার ও হাইজিন",
                description = "গোসল, ফেসওয়াশ ও সানস্ক্রিন বা ময়েশ্চারাইজার ব্যবহার।",
                category = MissionCategory.APPEARANCE,
                difficulty = MissionDifficulty.EASY,
                estimatedMinutes = 10,
                isShiftSafe = false,
                isCoreMission = false
            )
        )

        // মাইন্ড / লার্নিং
        missions.add(
            MissionItem(
                id = idCounter++,
                title = "১৫ মিনিট লার্নিং সেশন",
                description = "কোনো নতুন টেকনোলজি বা কাজের প্রয়োজনীয় বিষয় নিয়ে পড়ালেখা।",
                category = MissionCategory.MIND,
                difficulty = MissionDifficulty.MEDIUM,
                estimatedMinutes = 15,
                isShiftSafe = false,
                isCoreMission = false
            )
        )

        // নাইট রুটিন
        missions.add(
            MissionItem(
                id = idCounter++,
                title = "নাইট উইন্ড-ডাউন",
                description = "ঘুমানোর পূর্বে স্ক্রিন টাইম অফ এবং রুম অন্ধকার করা।",
                category = MissionCategory.SLEEP,
                difficulty = MissionDifficulty.MEDIUM,
                estimatedMinutes = 20,
                isShiftSafe = false,
                isCoreMission = true
            )
        )

        return missions
    }

    // "WHAT SHOULD I DO NOW?" — তাৎক্ষণিক পরবর্তী মিশন বের করা
    fun getNextMission(
        missions: List<MissionItem>,
        isDuringShift: Boolean
    ): MissionItem? {
        val pending = missions.filter { !it.isCompleted }

        // ডিউটির সময় হলে শুধুমাত্র শিফট-সেফ মিশনগুলো আগে দেখাবে
        return if (isDuringShift) {
            pending.firstOrNull { it.isShiftSafe } ?: pending.firstOrNull()
        } else {
            // ডিউটির বাইরে থাকলে কোর মিশনগুলো অগ্রাধিকার পাবে
            pending.firstOrNull { it.isCoreMission } ?: pending.firstOrNull()
        }
    }
}
