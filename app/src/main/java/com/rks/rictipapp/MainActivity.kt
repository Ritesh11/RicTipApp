package com.rks.rictipapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rks.rictipapp.ui.components.BillAmountComponent
import com.rks.rictipapp.ui.theme.RicTipAppTheme
import com.rks.rictipapp.ui.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RicTipAppTheme {
                Content()
            }
        }
    }
}

@Composable
fun Content(modifier: Modifier = Modifier) {
    var splitAmount by remember {
        mutableDoubleStateOf(100.00)
    }

    var numberOfPerson = remember {
        mutableStateOf(1)
    }

    var tipAmount = remember {
        mutableStateOf(0.0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        color = Color.White
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp)
        ) {
            SplitAmountContainer(splitAmount)
            Spacer(modifier = Modifier.height(10.dp))
            TipCalculator(modifier, numberOfPerson, tipAmount) { billAmt ->
                splitAmount = billAmt
                Log.d(MainActivity::class.java.name, "Content: $billAmt")
            }
        }
    }
}


@Composable
fun SplitAmountContainer(
    splitAmount: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        color = Color.Gray,
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val total = "%.2f".format(splitAmount)

            Text(
                text = "Total Per Person",
                modifier = modifier,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(
                modifier = modifier
                    .height(5.dp)
            )

            Text(
                text = "$${total}",
                modifier = modifier,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )

        }

    }
}


@Composable
fun TipCalculator(modifier: Modifier = Modifier,
                  numberOfPerson: MutableState<Int>,
                  tipAmount: MutableState<Double>,
    billAmountUpdated: (Double) -> Unit
) {
    val totalBillState = remember {
        mutableStateOf("")
    }

    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }

    var updatedAmount by remember {
        mutableDoubleStateOf(0.00)
    }



    var tipPercent by remember {
        mutableStateOf(0f)
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color = Color.Gray),
        color = Color.White
    ) {

        Column(
            modifier = modifier
                .padding(16.dp)
        ) {
            BillAmountComponent(valueState = totalBillState,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLined = true,
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    updatedAmount = totalBillState.value.trim().toDouble()
                    billAmountUpdated(updatedAmount)
                    keyboardController?.hide()
                }
            )

            if (validState) {
                SplitContainer(modifier) { onPersonUpdate ->
                    numberOfPerson.value = onPersonUpdate
                    var splitAmount = (totalBillState.value.trim().toInt().div(numberOfPerson.value));
                    tipAmount.value = "%.2f".format((splitAmount * tipPercent) / 100).toDouble()
                    updatedAmount = (splitAmount) + tipAmount.value
                    billAmountUpdated(updatedAmount)
                }

                TipContainer(modifier, tipAmount.value) { percent ->
                    tipPercent = percent
                    tipAmount.value = "%.2f".format((updatedAmount * percent) / 100).toDouble()
                    updatedAmount = (totalBillState.value.trim().toInt().div(numberOfPerson.value)) + tipAmount.value
                    billAmountUpdated(updatedAmount)

                }
            }

        }

    }

}

@Composable
fun SplitContainer(
    modifier: Modifier = Modifier,
    onPersonUpdate: (Int) -> Unit
) {

    var numberOfPerson by remember {
        mutableStateOf(1)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = "Split", modifier = modifier.weight(1f))

        Row(
            modifier = modifier
                .height(60.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(
                modifier = modifier,
                imageVector = Icons.Rounded.Remove,
                onClick = {
                    if (numberOfPerson > 1) {
                        numberOfPerson -= 1
                        onPersonUpdate(numberOfPerson)
                    }
                }, description = "Remove"
            )

            Spacer(modifier = modifier.width(10.dp))

            Text(text = "$numberOfPerson")

            Spacer(modifier = modifier.width(10.dp))

            RoundIconButton(
                modifier = modifier,
                imageVector = Icons.Rounded.Add,
                onClick = {
                    numberOfPerson += 1
                    onPersonUpdate(numberOfPerson)
                }, description = "Remove"
            )

        }

    }

}

@Composable
fun TipContainer(
    modifier: Modifier = Modifier,
    amt: Double,
    tipPresent: (Float) -> Unit
) {

    var tipPercent by remember {
        mutableStateOf(0f)
    }



    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = modifier
                .fillMaxWidth()
        ) {

            Text(
                text = "Tip",
                modifier = modifier
                    .weight(1f)
            )


            Text(
                text = "$${amt}",
                modifier = modifier
                    .weight(1f)
            )

        }

        Spacer(modifier = modifier.height(16.dp))

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "${tipPercent.toInt()}%")

            Spacer(modifier = modifier.height(16.dp))

            Slider(
                value = tipPercent,
                onValueChange = {
                    tipPercent = it
                    },
                valueRange = 0f..100f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                onValueChangeFinished = {
                    tipPresent(tipPercent)
                }
            )

        }


    }

}