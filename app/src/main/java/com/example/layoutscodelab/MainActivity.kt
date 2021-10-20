package com.example.layoutscodelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.layoutscodelab.ui.theme.LayoutsCodelabTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutsCodelabTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    LayoutsCodeLab()
                }
            }
        }
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // measure and position children given constraints logic here
        // Keep track of the width of each row
        val rowWidths = IntArray(rows) { 0 }

        // Keep track of the max height of each row
        val rowHeights = IntArray(rows) { 0 }

        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.mapIndexed { index, measurable ->

            // Measure each child
            val placeable = measurable.measure(constraints)

            // Track the width and max height of each row
            val row = index % rows
            rowWidths[row] += placeable.width
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)

            placeable
        }

        // Grid's width is the widest row
        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth)) ?: constraints.minWidth

        // Grid's height is the sum of the tallest element of each row
        // coerced to the height constraints
        val height = rowHeights.sumOf { it }
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        // Y of each row, based on the height accumulation of previous rows
        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i-1] + rowHeights[i-1]
        }

        // Set the size of the parent layout
        layout(width, height) {
            // x cord we have placed up to, per row
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows
                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )
                rowX[row] += placeable.width
            }
        }
    }
}

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border= BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp, 16.dp)
                    .background(color = MaterialTheme.colors.secondary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text)
        }
    }
}



@ExperimentalCoilApi
@Composable
fun LayoutsCodeLab() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text( text = "LayoutsCodelab" )
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            Modifier
                .padding(innerPadding)
                .padding(8.dp))
    }
}

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        // Check the composable has a first baseline
        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        // Height of the composable with padding - first baseline
        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(placeable.width, height) {
            // Where the composable gets placed
            placeable.placeRelative(0, placeableY)
        }
    }
)

@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // measure and position children give constraints logic here
        //Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each child
            measurable.measure(constraints)
        }
        
        // Track the y co-ord we have placed children up to
        var yPosition = 0
        
        //Set the size of the layout
        layout(constraints.maxWidth, constraints.maxHeight) {
            // place children in the parent layout
            placeables.forEach{ placeable ->  
                // Position item on the screen
                placeable.placeRelative(x=0, y=yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }


        }
    }
}

@ExperimentalCoilApi
@Composable
fun BodyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(8.dp)
    ) {
        Text( text = "Hi there!", Modifier.firstBaselineToTop(32.dp) )
        Text( text = "Thanks for playing!", Modifier.padding(top = 32.dp) )
        ImageList()
    }

}

@Composable
fun BodyContent2(modifier: Modifier =  Modifier) {
    MyOwnColumn(modifier.padding(8.dp)) {
        Text("MyOwnColumn", modifier.firstBaselineToTop(32.dp))
        Text("places items")
        Text("vertically.")
        Text("We've done it by hand", modifier.firstBaselineToTop(32.dp))

    }
}

@ExperimentalCoilApi
@Composable
fun ImageList() {

    val scrollState = rememberLazyListState()

    val listSize = 100

    val coroutineScope = rememberCoroutineScope()

    Row{
        Button(onClick = {
            coroutineScope.launch {
                scrollState.animateScrollToItem(0)
            }
        }) {
            Text("Scroll to the top")
        }

        Button(onClick = {
            coroutineScope.launch {
                scrollState.animateScrollToItem(listSize-1)
            }
        }) {
            Text("Scroll to the end")

        }
    }

    LazyColumn(state = scrollState) {
        items(100) {
            ImageListItem(index = it)
        }
    }
}

@ExperimentalCoilApi
@Composable
fun ImageListItem(index:Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Image(
            painter = rememberImagePainter(
                data = "https://developer.android.com/images/brand/Android_Robot.png"
            ),
            contentDescription = "Android Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text("item#$index", style = MaterialTheme.typography.subtitle1)
    }
}

@Composable
fun SimpleList() {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        items(100) {
            Text("Item #$it")
        }
    }
}

@Composable
fun PhotographerCard(modifier: Modifier = Modifier) {
    Row(
        modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.surface)
            .clickable(onClick = {/* Ignore onClick */ })
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        ) {
            // Image goes here
        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text("Alfred Sisley", fontWeight = FontWeight.Bold)
            // LocalContentAlpha is defining opacity level of its childre
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium
            ) {
                Text(
                    "3 minutes ago",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun ConstraintlayoutContent() {
    ConstraintLayout {

        // Create references for the composables to constrain
        val (button1, button2, text) = createRefs()

        Button(
            onClick = { },
            // Assign referenc "button to the Button composable
            // and constrain it to the top of ConstraintLayout
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button 1")
        }

        // Assign reference "text to the Text composable
        // and constrain it to the bottom of the Button composable
        Text("Text", Modifier.constrainAs(text) {
            top.linkTo(button1.bottom, margin = 16.dp)
            // Centers text horizontally in the ConstraintLayout
            centerAround(button1.end)
        })

        val barrier = createEndBarrier(button1, text)
        Button(
            onClick = { },
            modifier = Modifier.constrainAs(button2) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(barrier)
            }
        ) {
            Text("Button 2")
        }
    }
}

val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)


@Composable
fun BodyContent3(modifier: Modifier = Modifier) {
    StaggeredGrid(modifier = modifier, rows = 5) {
        for (topic in topics) {
            Chip(modifier = Modifier.padding(8.dp), text = topic)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConstraintPreview() {
    LayoutsCodelabTheme {
        ConstraintlayoutContent()
    }
}


@Preview(
    showBackground = true,
    name = "LayoutsCodelabPreview"
)
@Composable
fun LayoutsCodelabPreview() {
    LayoutsCodelabTheme {
        LayoutsCodeLab()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    LayoutsCodelabTheme {
        BodyContent2()
    }
}

@Preview
@Composable
fun ChipPreview() {
    LayoutsCodelabTheme {
        BodyContent3()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LayoutsCodelabTheme {
        PhotographerCard()
    }
}