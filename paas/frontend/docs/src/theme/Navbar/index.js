import React from 'react';
import NavbarLayout from '@theme/Navbar/Layout';
import NavbarContent from '@theme/Navbar/Content';
export default function Navbar() {
  if (typeof window !== 'undefined') {
    let footerComp = (<NavbarLayout>
      <NavbarContent />
    </NavbarLayout>)
    if (window.frames.length != parent.frames.length) {
      footerComp = (<div></div>)
    }
    return footerComp
  } else {
    return (
      <NavbarLayout>
      <NavbarContent />
    </NavbarLayout>
    );
  }
}
