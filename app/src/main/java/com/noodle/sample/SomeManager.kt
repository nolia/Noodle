package com.noodle.sample

import android.content.Context
import android.widget.Toast
import com.noodle.Noodle
import org.androidannotations.annotations.AfterInject
import org.androidannotations.annotations.EBean
import org.androidannotations.annotations.RootContext

/**
 * @author nikolaysoroka
 */
@EBean(scope = EBean.Scope.Singleton)
open class SomeManager {

  @RootContext
  lateinit internal var context: Context

  lateinit var noodle: Noodle

  @AfterInject
  fun afterInject() {
    noodle = Noodle()
  }

  fun saySomething() {
    Toast.makeText(context, "Hello! There!!!", Toast.LENGTH_SHORT).show()
  }
}