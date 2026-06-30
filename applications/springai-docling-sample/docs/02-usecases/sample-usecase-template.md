# Use Case: Insurance Claim Processing

**System:** Insurance Company

**Primary Actor:** Claimant

**Goal:** Get paid for car accident

---

## Main Success Scenario

1. Claimant submits claim with substantiating data.
2. Insurance company verifies claimant owns a valid policy.
3. Insurance company assigns agent to examine case.
4. Agent verifies all details are within policy guidelines.
5. Insurance company pays claimant.

---

## Extensions

### 1a. Submitted data is incomplete:
- **1a1.** Insurance company requests missing information.
- **1a2.** Claimant supplies missing information.

### 2a. Claimant does not own a valid policy:
- **2a1.** Insurance company declines claim, notifies claimant, records all this, terminates proceedings.

### 3a. No agents are available at this time:
- **3a1.** *(What does the insurance company do here?)*

### 4a. Accident violates basic policy guidelines:
- **4a1.** Insurance company declines claim, notifies claimant, records all this, terminates proceedings.

### 4b. Accident violates some minor policy guidelines:
- **4b1.** Insurance company begins negotiation with claimant as to degree of payment to be made.

---

## Variations

### 1. Claimant is:
- a) A person
- b) Another insurance company
- c) The government

### 5. Payment is:
- a) By check
- b) By interbank transfer
- c) By automatic prepayment of next installment
- d) By creation and payment of another policy