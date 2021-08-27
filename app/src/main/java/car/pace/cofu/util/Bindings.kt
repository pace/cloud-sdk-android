package car.pace.cofu.util

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import car.pace.cofu.R
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseRecyclerAdapter
import com.google.android.material.button.MaterialButton
import jp.wasabeef.recyclerview.animators.LandingAnimator

@BindingAdapter("android:text")
fun TextView.setTextRes(textRes: Int) {
    if (textRes > 0) setText(textRes) else text = ""
}

@BindingAdapter("htmlText")
fun TextView.setHtmlText(text: String?) {
    if (text == null) return
    setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT))
}

@BindingAdapter("drawableStart")
fun TextView.setStartDrawableRes(drawableRes: Int) {
    if (drawableRes == 0) return
    setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, drawableRes), null, null, null)
}


@BindingAdapter("visible", "invisibleInsteadOfGone", requireAll = false)
fun setVisibility(view: View, visible: Boolean, invisibleInsteadOfGone: Boolean = false) {
    view.visibility = when {
        visible -> View.VISIBLE
        invisibleInsteadOfGone -> View.INVISIBLE
        else -> View.GONE
    }
    (view.parent as? View)?.refreshDrawableState()
    view.refreshDrawableState()
}

@BindingAdapter("visibleIfGreaterThan0", "invisibleInsteadOfGone", requireAll = false)
fun setVisibilityForInts(view: View, compareInt: Int, invisibleInsteadOfGone: Boolean = false) {
    setVisibility(view, compareInt > 0, invisibleInsteadOfGone)
}


@BindingAdapter("imageRes")
fun ImageView.setImageByResource(@DrawableRes res: Int) {
    if (res != 0) setImageResource(res)
}

@BindingAdapter("items")
fun RecyclerView.setItems(_items: List<BaseItemViewModel>) {
    // copy list to make diff util work
    val items = _items.map { it }
    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    if (adapter is BaseRecyclerAdapter) {
        (adapter as? BaseRecyclerAdapter)?.setItems(items)
    } else {
        adapter = BaseRecyclerAdapter().apply {
            setItems(items)
        }
    }
}

const val ITEM_ANIMATION_LANDING = 1

@BindingAdapter("itemAnimation")
fun RecyclerView.setItemAnimation(animationIndex: Int) {
    when (animationIndex) {
        ITEM_ANIMATION_LANDING -> itemAnimator = LandingAnimator()
    }
}

@BindingAdapter("isRefreshing")
fun setSwipeRefreshLayoutState(view: SwipeRefreshLayout, isRefreshing: Boolean) {
    view.isRefreshing = isRefreshing
}

@BindingAdapter("icon")
fun MaterialButton.updateIcon(iconRes: Int) {
    if (iconRes != 0) setIconResource(iconRes)
}

/**
 * loads a http(s) url or a file from the assets. Put your assets in directories by language
 * (language keys in @string/assets_directory_name)
 */
@SuppressLint("SetJavaScriptEnabled")
@BindingAdapter("url")
fun loadFromUrl(view: WebView, url: String?) {
    with(view.settings) {
        javaScriptEnabled = true
        useWideViewPort = true
        view.isScrollbarFadingEnabled = true
    }

    view.webViewClient = object : WebViewClient() {

    }

    if (!url.isNullOrBlank()) {
        if (url.startsWith("http")) {
            view.loadUrl(url)
        } else {
            val lang = view.context.getString(R.string.assets_directory_name)
            view.loadUrl("file:///android_asset/$lang/$url")
        }
    }
}