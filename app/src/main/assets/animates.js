const tl = gsap.timeline({
    default: {duration: .75, ease: "Power3.easeOut"} //Set default timeline
})

tl.fromTo(".annimate-img",
 {scale: 1.4, 
    borderRadius: "0rem"},

 {scale: 1,
     borderRadius: "0rem", 
     delay: 0.25,
    duration: 2,
    ease: "elastic.out(2.5,2.5)"
    })

    tl.fromTo(".class1", {x: "100%" , opacity: .5}, {x:0, opacity: 1}, "<30%")
    tl.fromTo(".class2", {y: "100%" , opacity: .5}, {y:0, opacity: 1}, "<30%")
    tl.fromTo(".class3", {x: "-100%" , opacity: .5}, {x:0, opacity: 1}, "<30%")
    tl.fromTo(".class4", {x: "100%" , opacity: .5}, {x:0, opacity: 1}, "<30%")
    tl.fromTo(".class5", {x: "100%" , opacity: .5}, {x:0, opacity: 1}, "<30%")
    tl.fromTo(".class6", {y: "-100%" , opacity: .5}, {y:0, opacity: 1}, "<30%")
    

    console.log(letter)
    logo.textContent = ""

   