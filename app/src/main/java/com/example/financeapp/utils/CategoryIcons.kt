package com.example.financeapp.utils

import android.content.Context
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass

object CategoryIcons{
    private val list = listOf(
        "ic_category_airplane",
        "ic_category_account_balance_wallet",
        "ic_category_badge",
        "ic_category_domain",
        "ic_category_giftcard",
        "ic_category_car",
        "ic_category_games",
        "ic_category_plumbing",
        "ic_category_pets",
        "ic_category_cake",
        "ic_category_celebration",
        "ic_category_dining",
        "ic_category_exercise",
        "ic_category_subway",
        "ic_category_cases",
        "ic_category_card",
        "ic_category_card_travel",
        "ic_category_chef_hat",
        "ic_category_chess_pawn",
        "ic_category_cleaning_services",
        "ic_category_delivery_truck",
        "ic_category_dentistry",
        "ic_category_directions_boat",
        "ic_category_directions_car",
        "ic_category_eyeglasses",
        "ic_category_family_restroom",
        "ic_category_fastfood",
        "ic_category_forest",
        "ic_category_flight",
        "ic_category_footprint",
        "ic_category_garden_cart",
        "ic_category_handyman",
        "ic_category_headphones",
        "ic_category_healing",
        "ic_category_home_work",
        "ic_category_key_vertical",
        "ic_category_laptop",
        "ic_category_local_gas_station",
        "ic_category_local_laundry_service",
        "ic_category_local_pizza",
        "ic_category_medication",
        "ic_category_movie",
        "ic_category_money_bag",
        "ic_category_music_note",
        "ic_category_nutrition",
        "ic_category_palette",
        "ic_category_payments",
        "ic_category_pediatrics",
        "ic_category_phone_iphone",
        "ic_category_photo_camera",
        "ic_category_playing_cards",
        "ic_category_potted_plant",
        "ic_category_psychiatry",
        "ic_category_resource_public",
        "ic_category_raven",
        "ic_category_restaurant",
        "ic_category_sailing",
        "ic_category_savings",
        "ic_category_shopping_bag_speed",
        "ic_category_shopping_cart",
        "ic_category_school",
        "ic_category_sell",
        "ic_category_scooter",
        "ic_category_smart_toy",
        "ic_category_social_leaderboard",
        "ic_category_speed",
        "ic_category_sports_esports",
        "ic_category_store",
        "ic_category_storefront",
        "ic_category_theaters",
        "ic_category_train",
        "ic_category_two_wheeler",
        "ic_category_wallet",
        "ic_category_wifi"
    )

    val deleteIcon = R.drawable.ic_delete
    val editIcon = R.drawable.ic_edit
    val visibilityOnIcon = R.drawable.ic_visibility
    val visibilityOffIcon = R.drawable.ic_visibility_off
    val moreIcon = R.drawable.ic_more
    val addIcon = R.drawable.ic_simple_add
    val errorIcon = R.drawable.ic_error

    val addCardIcon = R.drawable.ic_add_card
    val switchMoneyIcon = R.drawable.ic_switch_money

    val notFound = R.drawable.ic_not_found


    fun getCategoryIcons() : List<String>{
        return list
    }

    fun getCategories(context: Context, count: Int = 0): List<CategoryClass> {
        return list
            .take(if (count == 0) list.size else count)
            .map { iconName ->
                val iconResId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                CategoryClass(
                    id = 0,
                    title = "",
                    colorId = "",
                    iconId = iconResId,
                    typeOperation = ""
                )
            }
    }

    fun getMoreIcon(context: Context): CategoryClass {
        return CategoryClass(
            id = 0,
            title = context.getString(R.string.more),
            typeOperation = "",
            iconId = moreIcon,
            colorId = "")
    }

    fun getAddIcon(context: Context): CategoryClass {
        return CategoryClass(
            id = 0,
            title = context.getString(R.string.add),
            typeOperation = "",
            iconId = addIcon,
            colorId = "")
    }

    fun getEmptyIcon(): Int{
        return notFound
    }
}